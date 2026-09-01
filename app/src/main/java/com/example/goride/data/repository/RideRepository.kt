package com.example.goride.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.goride.data.local.ActiveRideEntity
import com.example.goride.data.local.GoRideDatabase
import com.example.goride.data.local.RideEntity
import com.example.goride.data.model.Ride
import com.example.goride.data.model.Vehicle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Repository delle corse (RF10, RF11).
 *
 * Due sorgenti che lavorano insieme (Lez. 2.11):
 *  - Firestore e' la fonte di verita' remota
 *  - Room e' la cache locale, cosi' lo storico si vede anche offline
 *
 * La UI osserva SEMPRE Room. Il Repository sincronizza da Firestore
 * in background: e' il pattern "single source of truth", la lista non
 * sfarfalla e non resta mai vuota in attesa della rete.
 */
class RideRepository(context: Context) {

    private val db = FirebaseFirestore.getInstance()
    private val rideDao = GoRideDatabase.getDatabase(context).rideDao()
    private val activeRideDao = GoRideDatabase.getDatabase(context).activeRideDao()

    /**
     * RF10 - storico osservabile.
     *
     * Room aggiorna la LiveData da solo quando la tabella cambia.
     * map converte le entity in modelli di dominio: chi sta sopra
     * non vede mai RideEntity.
     */
    fun getStoricoCorse(userId: String): LiveData<List<Ride>> =
        rideDao.getCorseUtente(userId).map { entities ->
            entities.map { it.toRide() }
        }

    /** Scarica da Firestore e aggiorna la cache */
    suspend fun sincronizzaDaFirestore(userId: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            db.collection(COLLEZIONE_RIDES)
                .whereEqualTo(CAMPO_USER_ID, userId)
                .orderBy(CAMPO_START, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    val corse = snapshot.documents.mapNotNull { doc ->
                        try {
                            RideEntity(
                                id = doc.id,
                                userId = doc.getString(CAMPO_USER_ID).orEmpty(),
                                vehicleId = doc.getString(CAMPO_VEHICLE_ID).orEmpty(),
                                vehicleNome = doc.getString(CAMPO_VEHICLE_NOME).orEmpty(),
                                vehicleTipo = doc.getString(CAMPO_VEHICLE_TIPO) ?: "CLASSIC",
                                startTimestamp = doc.getLong(CAMPO_START) ?: 0L,
                                endTimestamp = doc.getLong(CAMPO_END),
                                minutiPrepagati = (doc.getLong(CAMPO_MINUTI) ?: 0L).toInt(),
                                tariffaSbloccoEuro = doc.getDouble(CAMPO_TARIFFA_SBLOCCO) ?: 0.0,
                                tariffaAlMinutoEuro = doc.getDouble(CAMPO_TARIFFA_MINUTO) ?: 0.0,
                                costoTotaleEuro = doc.getDouble(CAMPO_COSTO) ?: 0.0
                            )
                        } catch (e: Exception) {
                            // Un documento malformato non deve far fallire tutta la sincronizzazione
                            null
                        }
                    }
                    continuation.resume(Result.success(corse))
                }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }.fold(
            onSuccess = { corse ->
                rideDao.inserisciTutte(corse)
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )

    /**
     * Salva una corsa conclusa.
     *
     * Prima Room, poi Firestore: la corsa compare subito nello storico
     * anche se la rete e' lenta o assente.
     */
    suspend fun salvaCorsa(ride: Ride): Result<String> {
        rideDao.inserisci(RideEntity.fromRide(ride))

        return suspendCancellableCoroutine { continuation ->
            val documento = hashMapOf(
                CAMPO_USER_ID to ride.userId,
                CAMPO_VEHICLE_ID to ride.vehicleId,
                CAMPO_VEHICLE_NOME to ride.vehicleNome,
                CAMPO_VEHICLE_TIPO to ride.vehicleTipo.name,
                CAMPO_START to ride.startTimestamp,
                CAMPO_END to ride.endTimestamp,
                CAMPO_MINUTI to ride.minutiPrepagati,
                CAMPO_TARIFFA_SBLOCCO to ride.tariffaSbloccoEuro,
                CAMPO_TARIFFA_MINUTO to ride.tariffaAlMinutoEuro,
                CAMPO_COSTO to ride.costoTotaleEuro
            )

            db.collection(COLLEZIONE_RIDES)
                .document(ride.id)
                .set(documento)
                .addOnSuccessListener { continuation.resume(Result.success(ride.id)) }
                .addOnFailureListener { continuation.resume(Result.failure(it)) }
        }
    }

    suspend fun getCorsaById(id: String): Ride? =
        rideDao.getCorsaById(id)?.toRide()

    // ---------- Corsa attiva: ripristino dopo process death ----------

    suspend fun salvaCorsaAttiva(
        vehicle: Vehicle,
        startTimestamp: Long,
        scadenzaTimestamp: Long,
        minutiPrepagati: Int
    ) {
        activeRideDao.salva(
            ActiveRideEntity(
                vehicleId = vehicle.id,
                vehicleNome = vehicle.nome,
                vehicleTipo = vehicle.tipo.name,
                startTimestamp = startTimestamp,
                scadenzaTimestamp = scadenzaTimestamp,
                minutiPrepagati = minutiPrepagati,
                tariffaSbloccoEuro = vehicle.tariffaSbloccoEuro,
                tariffaAlMinutoEuro = vehicle.tariffaAlMinutoEuro
            )
        )
    }

    suspend fun getCorsaAttiva(): ActiveRideEntity? = activeRideDao.getCorsaAttiva()

    suspend fun cancellaCorsaAttiva() = activeRideDao.cancella()

    companion object {
        const val COLLEZIONE_RIDES = "rides"

        private const val CAMPO_USER_ID = "userId"
        private const val CAMPO_VEHICLE_ID = "vehicleId"
        private const val CAMPO_VEHICLE_NOME = "vehicleNome"
        private const val CAMPO_VEHICLE_TIPO = "vehicleTipo"
        private const val CAMPO_START = "startTimestamp"
        private const val CAMPO_END = "endTimestamp"
        private const val CAMPO_MINUTI = "minutiPrepagati"
        private const val CAMPO_TARIFFA_SBLOCCO = "tariffaSbloccoEuro"
        private const val CAMPO_TARIFFA_MINUTO = "tariffaAlMinutoEuro"
        private const val CAMPO_COSTO = "costoTotaleEuro"
    }
}