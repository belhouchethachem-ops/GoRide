package com.example.goride.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goride.data.model.ReviewType
import com.example.goride.data.repository.ReviewRepository
import com.example.goride.data.repository.UserRepository
import kotlinx.coroutines.launch

/** Pubblicazione di una recensione (RF13). */
class NewReviewViewModel : ViewModel() {

    private val reviewRepository = ReviewRepository()
    private val userRepository = UserRepository()

    val testo = MutableLiveData("")

    private val _tipoSelezionato = MutableLiveData(ReviewType.OPINIONE)
    val tipoSelezionato: LiveData<ReviewType> get() = _tipoSelezionato

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errore = MutableLiveData<String?>()
    val errore: LiveData<String?> get() = _errore

    private val _pubblicata = MutableLiveData(false)
    val pubblicata: LiveData<Boolean> get() = _pubblicata

    fun selezionaTipo(tipo: ReviewType) {
        _tipoSelezionato.value = tipo
    }

    fun pubblica() {
        val contenuto = testo.value.orEmpty().trim()

        when {
            contenuto.isEmpty() -> {
                _errore.value = "Scrivi qualcosa prima di pubblicare"
                return
            }
            contenuto.length < LUNGHEZZA_MINIMA -> {
                _errore.value = "La recensione è troppo corta"
                return
            }
        }

        viewModelScope.launch {
            _isLoading.value = true

            // Serve il nome dell'autore da mostrare nella lista
            val profilo = userRepository.getProfilo().getOrNull()
            val uid = userRepository.getUidCorrente()

            if (uid == null) {
                _errore.value = "Devi essere autenticato per pubblicare"
                _isLoading.value = false
                return@launch
            }

            reviewRepository.pubblicaRecensione(
                userId = uid,
                autoreNome = profilo?.nomeCompleto ?: "Utente GoRide",
                tipo = _tipoSelezionato.value ?: ReviewType.OPINIONE,
                testo = contenuto,
                vehicleId = null
            ).fold(
                onSuccess = { _pubblicata.value = true },
                onFailure = { _errore.value = "Pubblicazione non riuscita. Riprova." }
            )

            _isLoading.value = false
        }
    }

    fun erroreMostrato() { _errore.value = null }
    fun navigazioneCompletata() { _pubblicata.value = false }

    companion object {
        private const val LUNGHEZZA_MINIMA = 10
    }
}