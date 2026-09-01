package com.example.goride.data.repository

import com.example.goride.data.model.Vehicle
import com.example.goride.data.remote.MockVehicleDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Repository dei mezzi (Lez. 2.11).
 *
 * E' l'unica porta verso i dati: il ViewModel chiede i mezzi a
 * questa classe e non sa nulla di dove arrivino. Oggi e' un mock
 * in memoria, domani potrebbe essere Firestore o un'API REST:
 * cambierebbe solo questo file.
 *
 * Le funzioni sono suspend perche' in uno scenario reale sarebbero
 * chiamate di rete. Il chiamante le invoca da viewModelScope
 * (Lez. 2.9), quindi la UI non si blocca mai.
 */
class VehicleRepository {

    private val dataSource = MockVehicleDataSource

    /**
     * Mezzi disponibili nelle vicinanze (RF1, RF2, RF2.1, RF3).
     *
     * withContext(Dispatchers.IO) sposta il lavoro su un thread
     * secondario: con dati in memoria non servirebbe, ma teniamo
     * la forma corretta cosi' il passaggio a un backend vero non
     * richiederebbe modifiche.
     *
     * Il delay simula la latenza di rete: serve a rendere visibile
     * lo stato di caricamento nella UI durante lo sviluppo.
     */
    suspend fun getMezziDisponibili(): List<Vehicle> = withContext(Dispatchers.IO) {
        delay(RITARDO_SIMULATO_MS)
        dataSource.getMezziDisponibili()
    }

    /** Dettaglio di un singolo mezzo, null se non esiste */
    suspend fun getMezzoById(id: String): Vehicle? = withContext(Dispatchers.IO) {
        dataSource.getMezzoById(id)
    }

    /**
     * Sblocco del mezzo (RF4).
     *
     * Con un backend reale qui partirebbe la chiamata che apre il
     * lucchetto. Nel mock verifichiamo solo che il mezzo esista e
     * sia libero.
     *
     * Il Result di Kotlin porta con se' l'esito senza obbligare il
     * chiamante a gestire eccezioni: il ViewModel puo' distinguere
     * successo e fallimento in modo esplicito.
     */
    suspend fun sbloccaMezzo(vehicleId: String): Result<Vehicle> = withContext(Dispatchers.IO) {
        delay(RITARDO_SIMULATO_MS)

        val mezzo = dataSource.getMezzoById(vehicleId)

        when {
            mezzo == null ->
                Result.failure(IllegalArgumentException("Mezzo non trovato"))
            !mezzo.disponibile ->
                Result.failure(IllegalStateException("Il mezzo non e' piu' disponibile"))
            else ->
                Result.success(mezzo)
        }
    }

    companion object {
        /** Latenza finta, per vedere gli stati di caricamento in fase di sviluppo */
        private const val RITARDO_SIMULATO_MS = 600L
    }
}