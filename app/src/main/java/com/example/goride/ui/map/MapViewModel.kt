package com.example.goride.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goride.data.model.Vehicle
import com.example.goride.data.repository.VehicleRepository
import kotlinx.coroutines.launch

/**
 * ViewModel della schermata mappa (RF1, RF2, RF2.1, RF3).
 *
 * Lez. 2.8: espone lo stato alla View tramite LiveData e sopravvive
 * ai cambi di configurazione, quindi ruotando lo schermo i mezzi non
 * vengono ricaricati.
 *
 * Non conosce Android: niente Context, niente View, niente GoogleMap.
 * Sa solo di avere una lista di Vehicle.
 */
class  MapViewModel : ViewModel() {

    private val repository = VehicleRepository()

    // Lez. 2.8: la coppia MutableLiveData privata / LiveData pubblica.
    // La View puo' osservare ma non modificare: per cambiare stato
    // deve passare dai metodi di questa classe.

    private val _mezzi = MutableLiveData<List<Vehicle>>()
    val mezzi: LiveData<List<Vehicle>> get() = _mezzi

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    /** Messaggio di errore da mostrare in Snackbar; null = nessun errore */
    private val _errore = MutableLiveData<String?>()
    val errore: LiveData<String?> get() = _errore

    /** Mezzo selezionato dall'utente sulla mappa; null = nessuna selezione */
    private val _mezzoSelezionato = MutableLiveData<Vehicle?>()
    val mezzoSelezionato: LiveData<Vehicle?> get() = _mezzoSelezionato

    init {
        // I mezzi si caricano alla creazione del ViewModel, non a ogni
        // onCreateView del Fragment: dopo una rotazione l'istanza e' la
        // stessa e init non viene rieseguito.
        caricaMezzi()
    }

    /**
     * Carica i mezzi disponibili (RF1).
     *
     * viewModelScope (Lez. 2.9) lega la coroutine al ciclo di vita del
     * ViewModel: se viene distrutto, la coroutine viene cancellata da
     * sola. Nessun leak, nessun risultato che arriva a UI morta.
     */
    fun caricaMezzi() {
        viewModelScope.launch {
            _isLoading.value = true
            _errore.value = null

            try {
                _mezzi.value = repository.getMezziDisponibili()
            } catch (e: Exception) {
                _errore.value = "Impossibile caricare i mezzi. Riprova."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** L'utente ha toccato un marker: mostra il dettaglio (RF2.1, RF3) */
    fun selezionaMezzo(vehicle: Vehicle) {
        _mezzoSelezionato.value = vehicle
    }

    /** Il dettaglio e' stato chiuso */
    fun deselezionaMezzo() {
        _mezzoSelezionato.value = null
    }

    /**
     * Azzera l'errore dopo che la View lo ha mostrato.
     *
     * Senza questo, ruotando lo schermo l'observer riceverebbe di nuovo
     * l'ultimo valore e la Snackbar riapparirebbe: LiveData e' pensata
     * per lo stato, non per gli eventi una tantum.
     */
    fun erroreMostrato() {
        _errore.value = null
    }
}