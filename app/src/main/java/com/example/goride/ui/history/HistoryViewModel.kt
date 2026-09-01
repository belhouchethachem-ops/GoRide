package com.example.goride.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.goride.data.model.Ride
import com.example.goride.data.repository.RideRepository
import com.example.goride.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel dello storico (RF10).
 *
 * AndroidViewModel e non ViewModel: il RideRepository ha bisogno di
 * un Context per aprire Room. AndroidViewModel fornisce l'Application,
 * che e' sicura da tenere perche' vive quanto l'app.
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val rideRepository = RideRepository(application)
    private val userRepository = UserRepository()

    /**
     * La lista viene da Room, non da Firestore: si popola subito,
     * anche offline, e si aggiorna da sola quando la sincronizzazione
     * scrive nuovi dati.
     */
    val corse: LiveData<List<Ride>> =
        rideRepository.getStoricoCorse(userRepository.getUidCorrente().orEmpty())

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errore = MutableLiveData<String?>()
    val errore: LiveData<String?> get() = _errore

    init {
        sincronizza()
    }

    /** Scarica da Firestore e aggiorna la cache locale */
    fun sincronizza() {
        val uid = userRepository.getUidCorrente() ?: return

        viewModelScope.launch {
            _isLoading.value = true

            rideRepository.sincronizzaDaFirestore(uid).onFailure {
                // Fallimento silenzioso se abbiamo gia' dati locali:
                // l'utente vede lo storico in cache, non un errore
                if (corse.value.isNullOrEmpty()) {
                    _errore.value = "Impossibile aggiornare lo storico"
                }
            }

            _isLoading.value = false
        }
    }

    fun erroreMostrato() {
        _errore.value = null
    }
}