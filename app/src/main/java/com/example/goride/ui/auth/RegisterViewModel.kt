package com.example.goride.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goride.data.repository.UserRepository
import kotlinx.coroutines.launch

/** ViewModel della registrazione (RF8). */
class RegisterViewModel : ViewModel() {

    private val repository = UserRepository()

    // Two-way binding con i campi del form
    val nome = MutableLiveData("")
    val cognome = MutableLiveData("")
    val email = MutableLiveData("")
    val telefono = MutableLiveData("")
    val password = MutableLiveData("")
    val confermaPassword = MutableLiveData("")

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errore = MutableLiveData<String?>()
    val errore: LiveData<String?> get() = _errore

    private val _registrazioneRiuscita = MutableLiveData(false)
    val registrazioneRiuscita: LiveData<Boolean> get() = _registrazioneRiuscita

    fun registra() {
        val n = nome.value.orEmpty().trim()
        val c = cognome.value.orEmpty().trim()
        val e = email.value.orEmpty().trim()
        val t = telefono.value.orEmpty().trim()
        val p = password.value.orEmpty()
        val cp = confermaPassword.value.orEmpty()

        val erroreValidazione = valida(n, c, e, t, p, cp)
        if (erroreValidazione != null) {
            _errore.value = erroreValidazione
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errore.value = null

            repository.registra(n, c, e, t, p).fold(
                onSuccess = { _registrazioneRiuscita.value = true },
                onFailure = { _errore.value = traduciErrore(it) }
            )

            _isLoading.value = false
        }
    }

    /** Validazione lato client: evita chiamate di rete inutili */
    private fun valida(
        nome: String, cognome: String, email: String,
        telefono: String, password: String, conferma: String
    ): String? = when {
        nome.isEmpty() -> "Inserisci il nome"
        cognome.isEmpty() -> "Inserisci il cognome"
        email.isEmpty() -> "Inserisci l'email"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email non valida"
        telefono.isEmpty() -> "Inserisci il telefono"
        telefono.length < 8 -> "Numero di telefono troppo corto"
        password.length < 6 -> "La password deve avere almeno 6 caratteri"
        password != conferma -> "Le password non coincidono"
        else -> null
    }

    private fun traduciErrore(eccezione: Throwable): String {
        val m = eccezione.message.orEmpty()
        return when {
            m.contains("already in use", ignoreCase = true) ->
                "Esiste già un account con questa email"
            m.contains("badly formatted", ignoreCase = true) ->
                "Email non valida"
            m.contains("network", ignoreCase = true) ->
                "Nessuna connessione. Controlla la rete."
            else -> "Registrazione non riuscita. Riprova."
        }
    }

    fun erroreMostrato() { _errore.value = null }
    fun navigazioneCompletata() { _registrazioneRiuscita.value = false }
}