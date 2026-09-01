package com.example.goride.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Tipo di recensione (RF13).
 * Determina il layout nella lista, via getItemViewType
 * del ListAdapter (Lez. 2.5).
 */
enum class ReviewType {
    GUASTO,
    OPINIONE,
    CONSIGLIO
}

@Parcelize
data class Review(
    val id: String,
    val userId: String,
    val autoreNome: String,
    val tipo: ReviewType,
    val testo: String,
    val timestamp: Long,
    val vehicleId: String?
) : Parcelable
