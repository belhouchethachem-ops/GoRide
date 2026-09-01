package com.example.goride.ui.ride

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.goride.data.model.Ride
import com.example.goride.data.model.Vehicle
import com.example.goride.data.model.VehicleType
import com.example.goride.data.repository.RideRepository
import com.example.goride.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

/**
 * ViewModel della corsa (RF5, RF6, RF6.1, RF6.2).
 *
 * Da istanziare con by activityViewModels(): deve sopravvivere alla
 * navigazione tra Fragment, non solo alla rotazione.
 *
 * AndroidViewModel perche' il RideRepository ha bisogno di un Context
 * per aprire Room.
 *
 * PUNTO CHIAVE: il timer non decrementa una variabile, ma ricalcola
 * ogni secondo la differenza tra un timestamp di scadenza e l'ora
 * corrente. Cosi' il tempo resta corretto anche se l'app va in
 * background, dove le coroutine possono essere sospese.
 */
class RideViewModel(application: Application) : AndroidViewModel(application) {

    private val rideRepository = RideRepository(application)
    private val userRepository = UserRepository()

    private var mezzo: Vehicle? = null
    private var startTimestamp: Long = 0L
    private var scadenzaTimestamp: Long = 0L
    private var minutiPrepagatiTotali: Int = 0

    private var jobTimer: Job? = null

    private val _millisecondiRimanenti = MutableLiveData(0L)
    val millisecondiRimanenti: LiveData<Long> get() = _millisecondiRimanenti

    /**
     * Lez. 2.8 - Transformations: il tempo formattato deriva dai
     * millisecondi, non e' uno stato separato da tenere sincronizzato.
     */
    val tempoFormattato: LiveData<String> = _millisecondiRimanenti.map { millis ->
        val totaleSecondi = (millis / 1000).coerceAtLeast(0)
        val minuti = totaleSecondi / 60
        val secondi = totaleSecondi % 60
        String.format(Locale.ITALY, "%02d:%02d", minuti, secondi)
    }

    private val _corsaAttiva = MutableLiveData(false)
    val corsaAttiva: LiveData<Boolean> get() = _corsaAttiva

    /** Scatta a zero: il Fragment lo osserva per mostrare l'alert (RF6) */
    private val _tempoScaduto = MutableLiveData(false)
    val tempoScaduto: LiveData<Boolean> get() = _tempoScaduto

    private val _costoAttuale = MutableLiveData(0.0)
    val costoAttuale: LiveData<Double> get() = _costoAttuale

    private val _corsaTerminata = MutableLiveData(false)
    val corsaTerminata: LiveData<Boolean> get() = _corsaTerminata

    val mezzoCorrente: Vehicle? get() = mezzo

    // -----------------------------------------------------------
    //  AVVIO
    // -----------------------------------------------------------

    /**
     * RF4/RF5 - avvia la corsa.
     *
     * Il controllo su corsaAttiva evita che una rotazione durante il
     * primo frame faccia ripartire tutto da capo.
     */
    fun avviaCorsa(vehicle: Vehicle, minutiPrepagati: Int) {
        if (_corsaAttiva.value == true) return

        mezzo = vehicle
        startTimestamp = System.currentTimeMillis()
        minutiPrepagatiTotali = minutiPrepagati
        scadenzaTimestamp = startTimestamp + minutiPrepagati * MILLIS_PER_MINUTO

        _corsaAttiva.value = true
        _tempoScaduto.value = false
        _corsaTerminata.value = false

        salvaStatoSuRoom()
        avviaTimer()
    }

    /**
     * Ripristina una corsa interrotta dalla morte del processo.
     * Chiamato dal RideFragment prima di chiedere la durata.
     *
     * @return true se c'era una corsa da riprendere
     */
    suspend fun ripristinaCorsaAttiva(): Boolean {
        val salvata = rideRepository.getCorsaAttiva() ?: return false

        // Scaduta da troppo tempo: la consideriamo abbandonata
        if (salvata.scadenzaTimestamp < System.currentTimeMillis() - TOLLERANZA_MS) {
            rideRepository.cancellaCorsaAttiva()
            return false
        }

        // Ricostruiamo il mezzo dai dati salvati. lat/lon e batteria
        // non servono durante la corsa, quindi restano a valori neutri.
        mezzo = Vehicle(
            id = salvata.vehicleId,
            nome = salvata.vehicleNome,
            tipo = VehicleType.valueOf(salvata.vehicleTipo),
            lat = 0.0,
            lon = 0.0,
            batteriaPercentuale = null,
            autonomiaKm = null,
            tariffaSbloccoEuro = salvata.tariffaSbloccoEuro,
            tariffaAlMinutoEuro = salvata.tariffaAlMinutoEuro,
            disponibile = false
        )

        startTimestamp = salvata.startTimestamp
        scadenzaTimestamp = salvata.scadenzaTimestamp
        minutiPrepagatiTotali = salvata.minutiPrepagati

        _corsaAttiva.value = true
        _tempoScaduto.value = false
        _corsaTerminata.value = false

        avviaTimer()
        return true
    }

    // -----------------------------------------------------------
    //  TIMER
    // -----------------------------------------------------------

    /**
     * Il cuore del meccanismo.
     *
     * Ogni secondo ricalcola scadenza - adesso. Se l'app resta in
     * background cinque minuti, al ritorno il valore e' corretto:
     * non accumuliamo tick, leggiamo l'orologio.
     */
    private fun avviaTimer() {
        jobTimer?.cancel()

        jobTimer = viewModelScope.launch {
            while (isActive) {
                val rimanenti = scadenzaTimestamp - System.currentTimeMillis()

                _millisecondiRimanenti.value = rimanenti.coerceAtLeast(0)
                aggiornaCosto()

                if (rimanenti <= 0) {
                    _tempoScaduto.value = true
                    break
                }

                delay(INTERVALLO_TICK_MS)
            }
        }
    }

    /** Costo maturato: sblocco + minuti trascorsi, arrotondati per eccesso */
    private fun aggiornaCosto() {
        val vehicle = mezzo ?: return

        val millisTrascorsi = System.currentTimeMillis() - startTimestamp
        val minutiTrascorsi = Math.ceil(millisTrascorsi.toDouble() / MILLIS_PER_MINUTO).toInt()

        _costoAttuale.value =
            vehicle.tariffaSbloccoEuro + minutiTrascorsi * vehicle.tariffaAlMinutoEuro
    }

    // -----------------------------------------------------------
    //  ESTENSIONE E CHIUSURA
    // -----------------------------------------------------------

    /**
     * RF6.1 - estensione.
     *
     * Sposta in avanti la scadenza invece di ricominciare: se l'utente
     * estende con 10 secondi ancora disponibili, quelli non si perdono.
     */
    fun estendiCorsa(minutiAggiuntivi: Int) {
        val base = maxOf(scadenzaTimestamp, System.currentTimeMillis())
        scadenzaTimestamp = base + minutiAggiuntivi * MILLIS_PER_MINUTO
        minutiPrepagatiTotali += minutiAggiuntivi

        _tempoScaduto.value = false

        salvaStatoSuRoom()
        avviaTimer()
    }

    /** RF6.2 - termina la corsa e la salva nello storico */
    fun terminaCorsa() {
        jobTimer?.cancel()
        jobTimer = null

        aggiornaCosto()

        val vehicle = mezzo
        val uid = userRepository.getUidCorrente()

        if (vehicle != null && uid != null) {
            val corsa = Ride(
                id = UUID.randomUUID().toString(),
                userId = uid,
                vehicleId = vehicle.id,
                vehicleNome = vehicle.nome,
                vehicleTipo = vehicle.tipo,
                startTimestamp = startTimestamp,
                endTimestamp = System.currentTimeMillis(),
                minutiPrepagati = minutiPrepagatiTotali,
                tariffaSbloccoEuro = vehicle.tariffaSbloccoEuro,
                tariffaAlMinutoEuro = vehicle.tariffaAlMinutoEuro,
                costoTotaleEuro = _costoAttuale.value ?: 0.0
            )

            viewModelScope.launch {
                // Prima Room, poi Firestore: la corsa compare subito
                // nello storico anche senza rete
                rideRepository.salvaCorsa(corsa)
                rideRepository.cancellaCorsaAttiva()
            }
        }

        _corsaAttiva.value = false
        _tempoScaduto.value = false
        _corsaTerminata.value = true
    }

    // -----------------------------------------------------------
    //  SUPPORTO
    // -----------------------------------------------------------

    /** Persiste lo stato corrente, per il ripristino dopo process death */
    private fun salvaStatoSuRoom() {
        val vehicle = mezzo ?: return

        viewModelScope.launch {
            rideRepository.salvaCorsaAttiva(
                vehicle,
                startTimestamp,
                scadenzaTimestamp,
                minutiPrepagatiTotali
            )
        }
    }

    fun alertMostrato() {
        _tempoScaduto.value = false
    }

    fun navigazioneCompletata() {
        _corsaTerminata.value = false
    }

    /**
     * Lez. 2.8 - onCleared: ultima chiamata prima della distruzione.
     * viewModelScope cancella gia' le coroutine da solo, ma essere
     * espliciti non fa male.
     */
    override fun onCleared() {
        super.onCleared()
        jobTimer?.cancel()
    }

    companion object {
        private const val MILLIS_PER_MINUTO = 60_000L
        private const val INTERVALLO_TICK_MS = 1000L

        /** Oltre 30 minuti dalla scadenza, la corsa e' considerata abbandonata */
        private const val TOLLERANZA_MS = 30 * 60_000L

        /** Opzioni di acquisto, in minuti */
        val DURATE_DISPONIBILI = listOf(15, 30, 60)
        val ESTENSIONI_DISPONIBILI = listOf(5, 15, 30)
    }
}