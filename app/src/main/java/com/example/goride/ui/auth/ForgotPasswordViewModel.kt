package com.example.goride.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.goride.data.repository.UserRepository
import kotlinx.coroutines.launch

/** Recupero password (RF7.1). La mail la invia Firebase. */
class ForgotPasswordViewModel : ViewModel() {

    private val repository = UserRepository()

    val email = MutableLiveData("")

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _messaggio = MutableLiveData<String?>()
    val messaggio: LiveData<String?> get() = _messaggio

    private val _emailInviata = MutableLiveData(false)
    val emailInviata: LiveData<Boolean> get() = _emailInviata

    fun inviaEmail() {
        val indirizzo = email.value.orEmpty().trim()

        if (indirizzo.isEmpty() ||
            !android.util.Patterns.EMAIL_ADDRESS.matcher(indirizzo).matches()
        ) {
            _messaggio.value = "Inserisci un'email valida"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            repository.inviaEmailRecupero(indirizzo).fold(
                onSuccess = {
                    // Messaggio volutamente generico: non riveliamo se
                    // l'email esiste o meno nel sistema
                    _messaggio.value = "Se l'email è registrata, riceverai il link di recupero"
                    _emailInviata.value = true
                },
                onFailure = {
                    _messaggio.value = "Invio non riuscito. Riprova."
                }
            )

            _isLoading.value = false
        }
    }

    fun messaggioMostrato() { _messaggio.value = null }
}