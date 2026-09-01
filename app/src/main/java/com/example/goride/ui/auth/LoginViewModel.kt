package com.example.goride.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goride.data.repository.UserRepository
import kotlinx.coroutines.launch

/**
 * ViewModel del login (RF7).
 *
 * email e password sono MutableLiveData pubbliche perche' il layout
 * le lega in two-way binding con @={}: l'utente scrive nel campo e
 * il valore arriva qui senza una riga di codice nel Fragment
 * (Lez. 2.8).
 *
 * Gli altri stati restano incapsulati con la coppia privata/pubblica.
 */
class LoginViewModel : ViewModel() {

    private val repository = UserRepository()

    // Two-way binding: eccezione consapevole alla regola dell'incapsulamento
    val email = MutableLiveData("")
    val password = MutableLiveData("")

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errore = MutableLiveData<String?>()
    val errore: LiveData<String?> get() = _errore

    /** Diventa true a login riuscito: il Fragment lo osserva per navigare */
    private val _loginRiuscito = MutableLiveData(false)
    val loginRiuscito: LiveData<Boolean> get() = _loginRiuscito

    /** RF7 - login */
    fun login() {
        val emailInserita = email.value.orEmpty().trim()
        val passwordInserita = password.value.orEmpty()

        // Validazione lato client: evita chiamate di rete inutili
        when {
            emailInserita.isEmpty() -> {
                _errore.value = "Inserisci l'email"
                return
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(emailInserita).matches() -> {
                _errore.value = "Email non valida"
                return
            }
            passwordInserita.isEmpty() -> {
                _errore.value = "Inserisci la password"
                return
            }
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errore.value = null

            val esito = repository.login(emailInserita, passwordInserita)

            esito.fold(
                onSuccess = {
                    _loginRiuscito.value = true
                },
                onFailure = { eccezione ->
                    // I messaggi di Firebase sono in inglese e tecnici:
                    // li traduciamo in qualcosa di leggibile
                    _errore.value = traduciErrore(eccezione)
                }
            )

            _isLoading.value = false
        }
    }

    private fun traduciErrore(eccezione: Throwable): String {
        val messaggio = eccezione.message.orEmpty()
        return when {
            messaggio.contains("password is invalid", ignoreCase = true) ||
                    messaggio.contains("credential is incorrect", ignoreCase = true) ->
                "Email o password errate"
            messaggio.contains("no user record", ignoreCase = true) ->
                "Nessun account con questa email"
            messaggio.contains("network", ignoreCase = true) ->
                "Nessuna connessione. Controlla la rete."
            messaggio.contains("blocked all requests", ignoreCase = true) ->
                "Troppi tentativi. Riprova più tardi."
            else -> "Accesso non riuscito. Riprova."
        }
    }

    fun erroreMostrato() {
        _errore.value = null
    }

    fun navigazioneCompletata() {
        _loginRiuscito.value = false
    }
}