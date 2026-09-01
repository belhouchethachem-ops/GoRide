package com.example.goride.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.goride.data.model.Review
import com.example.goride.data.repository.ReviewRepository

/**
 * ViewModel della community (RF12, RF14).
 *
 * La lista arriva dal listener realtime: nessun metodo di refresh,
 * si aggiorna da sola.
 */
class CommunityViewModel : ViewModel() {

    private val repository = ReviewRepository()

    val recensioni: LiveData<List<Review>> = repository.recensioni

    init {
        repository.iniziaAscolto()
    }

    /**
     * Lez. 2.8 - onCleared: qui stacchiamo il listener di Firestore.
     * Dimenticarlo significherebbe tenere aperta una connessione per
     * tutta la vita dell'app.
     */
    override fun onCleared() {
        super.onCleared()
        repository.fermaAscolto()
    }
}