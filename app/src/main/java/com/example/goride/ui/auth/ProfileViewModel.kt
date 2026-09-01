package com.example.goride.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goride.data.model.User
import com.example.goride.data.repository.UserRepository
import kotlinx.coroutines.launch

/** Profilo utente: visualizzazione e modifica (RF8.1). */
class ProfileViewModel : ViewModel() {

    private val repository = UserRepository()

    // Two-way binding con i campi modificabili
    val nome = MutableLiveData("")
    val cognome = MutableLiveData("")
    val telefono = MutableLiveData("")

    private val _utente = MutableLiveData<User?>()
    val utente: LiveData<User?> get() = _utente

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _messaggio = MutableLiveData<String?>()
    val messaggio: LiveData<String?> get() = _messaggio

    private val _logoutEseguito = MutableLiveData(false)
    val logoutEseguito: LiveData<Boolean> get() = _logoutEseguito

    init {
        caricaProfilo()
    }

    private fun caricaProfilo() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getProfilo().fold(
                onSuccess = { user ->
                    _utente.value = user
                    // Precompiliamo i campi con i valori attuali
                    nome.value = user.nome
                    cognome.value = user.cognome
                    telefono.value = user.telefono
                },
                onFailure = {
                    _messaggio.value = "Impossibile caricare il profilo"
                }
            )

            _isLoading.value = false
        }
    }

    /** RF8.1 - salva le modifiche */
    fun salvaModifiche() {
        val n = nome.value.orEmpty().trim()
        val c = cognome.value.orEmpty().trim()
        val t = telefono.value.orEmpty().trim()

        when {
            n.isEmpty() -> { _messaggio.value = "Il nome non può essere vuoto"; return }
            c.isEmpty() -> { _messaggio.value = "Il cognome non può essere vuoto"; return }
            t.length < 8 -> { _messaggio.value = "Numero di telefono non valido"; return }
        }

        viewModelScope.launch {
            _isLoading.value = true

            repository.aggiornaProfilo(n, c, t).fold(
                onSuccess = {
                    _messaggio.value = "Profilo aggiornato"
                    caricaProfilo()
                },
                onFailure = {
                    _messaggio.value = "Salvataggio non riuscito"
                }
            )

            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _logoutEseguito.value = true
    }

    fun messaggioMostrato() { _messaggio.value = null }
    fun navigazioneCompletata() { _logoutEseguito.value = false }
}