package com.example.goride.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.goride.data.model.Review
import com.example.goride.data.model.ReviewType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Repository delle recensioni (RF12, RF13, RF14).
 *
 * Qui sfruttiamo il listener realtime di Firestore: quando un altro
 * utente pubblica, la lista si aggiorna da sola senza refresh.
 *
 * Il ponte tra callback e LiveData e' il pattern Observer della
 * Lez. 2.8 applicato letteralmente: Firestore notifica, noi scriviamo
 * nella MutableLiveData, la UI reagisce.
 */
class ReviewRepository {

    private val db = FirebaseFirestore.getInstance()

    private val _recensioni = MutableLiveData<List<Review>>()
    val recensioni: LiveData<List<Review>> get() = _recensioni

    private var listener: ListenerRegistration? = null

    /**
     * RF14 - avvia l'ascolto realtime.
     *
     * addSnapshotListener resta attivo finche' non lo rimuoviamo:
     * ogni modifica alla collezione arriva qui automaticamente.
     */
    fun iniziaAscolto() {
        listener?.remove()

        listener = db.collection(COLLEZIONE_REVIEWS)
            .orderBy(CAMPO_TIMESTAMP, Query.Direction.DESCENDING)
            .limit(LIMITE_RECENSIONI)
            .addSnapshotListener { snapshot, errore ->
                if (errore != null || snapshot == null) return@addSnapshotListener

                _recensioni.value = snapshot.documents.mapNotNull { doc ->
                    try {
                        Review(
                            id = doc.id,
                            userId = doc.getString(CAMPO_USER_ID).orEmpty(),
                            autoreNome = doc.getString(CAMPO_AUTORE).orEmpty(),
                            tipo = ReviewType.valueOf(
                                doc.getString(CAMPO_TIPO) ?: ReviewType.OPINIONE.name
                            ),
                            testo = doc.getString(CAMPO_TESTO).orEmpty(),
                            timestamp = doc.getLong(CAMPO_TIMESTAMP) ?: 0L,
                            vehicleId = doc.getString(CAMPO_VEHICLE_ID)
                        )
                    } catch (e: Exception) {
                        // Un documento malformato non deve far sparire tutta la lista
                        null
                    }
                }
            }
    }

    /**
     * Il listener va rimosso quando la schermata muore, altrimenti
     * resta attivo a consumare quota e memoria.
     */
    fun fermaAscolto() {
        listener?.remove()
        listener = null
    }

    /** RF13 - pubblica una recensione */
    suspend fun pubblicaRecensione(
        userId: String,
        autoreNome: String,
        tipo: ReviewType,
        testo: String,
        vehicleId: String?
    ): Result<String> = suspendCancellableCoroutine { continuation ->

        val id = UUID.randomUUID().toString()

        val documento = hashMapOf(
            CAMPO_USER_ID to userId,
            CAMPO_AUTORE to autoreNome,
            CAMPO_TIPO to tipo.name,
            CAMPO_TESTO to testo,
            CAMPO_TIMESTAMP to System.currentTimeMillis(),
            CAMPO_VEHICLE_ID to vehicleId
        )

        db.collection(COLLEZIONE_REVIEWS)
            .document(id)
            .set(documento)
            .addOnSuccessListener { continuation.resume(Result.success(id)) }
            .addOnFailureListener { continuation.resume(Result.failure(it)) }
    }

    companion object {
        const val COLLEZIONE_REVIEWS = "reviews"

        private const val LIMITE_RECENSIONI = 100L

        private const val CAMPO_USER_ID = "userId"
        private const val CAMPO_AUTORE = "autoreNome"
        private const val CAMPO_TIPO = "tipo"
        private const val CAMPO_TESTO = "testo"
        private const val CAMPO_TIMESTAMP = "timestamp"
        private const val CAMPO_VEHICLE_ID = "vehicleId"
    }
}