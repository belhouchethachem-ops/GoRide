package com.example.goride.data.repository

import com.example.goride.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Repository dell'utente (RF7, RF7.1, RF8, RF8.1).
 *
 * Unica classe che parla con FirebaseAuth e con la collezione users.
 * ViewModel e Fragment non vedono mai FirebaseUser o DocumentSnapshot:
 * ricevono solo il modello di dominio User.
 *
 * Le API di Firebase sono a callback (addOnSuccessListener), mentre il
 * corso usa le coroutine. Il ponte lo costruiamo con
 * suspendCancellableCoroutine, senza aggiungere dipendenze esterne.
 */
class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /** true se c'e' una sessione attiva: decide la schermata iniziale */
    fun isLoggato(): Boolean = auth.currentUser != null

    fun getUidCorrente(): String? = auth.currentUser?.uid

    /** RF7 - login con email e password */
    suspend fun login(email: String, password: String): Result<String> =
        suspendCancellableCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { risultato ->
                    val uid = risultato.user?.uid
                    if (uid != null) {
                        continuation.resume(Result.success(uid))
                    } else {
                        continuation.resume(
                            Result.failure(IllegalStateException("Utente non valido"))
                        )
                    }
                }
                .addOnFailureListener { errore ->
                    continuation.resume(Result.failure(errore))
                }
        }

    /**
     * RF8 - registrazione.
     *
     * Due operazioni in sequenza: crea l'account in Authentication,
     * poi salva il profilo in Firestore. La password resta solo in
     * Authentication, mai in Firestore.
     */
    suspend fun registra(
        nome: String,
        cognome: String,
        email: String,
        telefono: String,
        password: String
    ): Result<String> = suspendCancellableCoroutine { continuation ->
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { risultato ->
                val uid = risultato.user?.uid
                if (uid == null) {
                    continuation.resume(
                        Result.failure(IllegalStateException("Registrazione fallita"))
                    )
                    return@addOnSuccessListener
                }

                val profilo = hashMapOf(
                    CAMPO_NOME to nome,
                    CAMPO_COGNOME to cognome,
                    CAMPO_EMAIL to email,
                    CAMPO_TELEFONO to telefono,
                    CAMPO_DATA_REGISTRAZIONE to System.currentTimeMillis()
                )

                db.collection(COLLEZIONE_USERS).document(uid)
                    .set(profilo)
                    .addOnSuccessListener { continuation.resume(Result.success(uid)) }
                    .addOnFailureListener { continuation.resume(Result.failure(it)) }
            }
            .addOnFailureListener { errore ->
                continuation.resume(Result.failure(errore))
            }
    }

    /** RF7.1 - recupero password: la mail la manda Firebase */
    suspend fun inviaEmailRecupero(email: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }

    /** Profilo dell'utente loggato (RF8.1) */
    suspend fun getProfilo(): Result<User> = suspendCancellableCoroutine { continuation ->
        val uid = auth.currentUser?.uid
        if (uid == null) {
            continuation.resume(Result.failure(IllegalStateException("Nessun utente loggato")))
            return@suspendCancellableCoroutine
        }

        db.collection(COLLEZIONE_USERS).document(uid)
            .get()
            .addOnSuccessListener { documento ->
                if (documento.exists()) {
                    val user = User(
                        uid = uid,
                        nome = documento.getString(CAMPO_NOME).orEmpty(),
                        cognome = documento.getString(CAMPO_COGNOME).orEmpty(),
                        email = documento.getString(CAMPO_EMAIL).orEmpty(),
                        telefono = documento.getString(CAMPO_TELEFONO).orEmpty(),
                        dataRegistrazione = documento.getLong(CAMPO_DATA_REGISTRAZIONE) ?: 0L
                    )
                    continuation.resume(Result.success(user))
                } else {
                    continuation.resume(
                        Result.failure(IllegalStateException("Profilo non trovato"))
                    )
                }
            }
            .addOnFailureListener { continuation.resume(Result.failure(it)) }
    }

    /** RF8.1 - modifica dei dati anagrafici (non email ne' password) */
    suspend fun aggiornaProfilo(
        nome: String,
        cognome: String,
        telefono: String
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val uid = auth.currentUser?.uid
        if (uid == null) {
            continuation.resume(Result.failure(IllegalStateException("Nessun utente loggato")))
            return@suspendCancellableCoroutine
        }

        val aggiornamenti = mapOf(
            CAMPO_NOME to nome,
            CAMPO_COGNOME to cognome,
            CAMPO_TELEFONO to telefono
        )

        db.collection(COLLEZIONE_USERS).document(uid)
            .update(aggiornamenti)
            .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
            .addOnFailureListener { continuation.resume(Result.failure(it)) }
    }

    fun logout() {
        auth.signOut()
    }

    companion object {
        const val COLLEZIONE_USERS = "users"

        private const val CAMPO_NOME = "nome"
        private const val CAMPO_COGNOME = "cognome"
        private const val CAMPO_EMAIL = "email"
        private const val CAMPO_TELEFONO = "telefono"
        private const val CAMPO_DATA_REGISTRAZIONE = "dataRegistrazione"
    }
}