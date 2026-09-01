package com.example.goride.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Profilo utente (RF8, RF8.1).
 * La password NON compare qui: la gestisce solo FirebaseAuth.
 */
@Parcelize
data class User(
    val uid: String,
    val nome: String,
    val cognome: String,
    val email: String,
    val telefono: String,
    val dataRegistrazione: Long
) : Parcelable {

    val nomeCompleto: String
        get() = "$nome $cognome"
}
