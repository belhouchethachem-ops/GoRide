package com.example.goride.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Tipologia di mezzo (RF2).
 * Una enum invece di una String: il compilatore impedisce
 * di scrivere un tipo inesistente.
 */
enum class VehicleType {
    CLASSIC,
    EBIKE
}

/**
 * Un mezzo disponibile al noleggio.
 *
 * Parcelable perche' viaggia tra Fragment via Safe Args
 * (Lez. 2.4: "tra le due e' da preferire Parcelable").
 *
 * batteria e autonomia sono nullable: hanno senso solo
 * per le e-bike (RF2.1).
 */
@Parcelize
data class Vehicle(
    val id: String,
    val nome: String,
    val tipo: VehicleType,
    val lat: Double,
    val lon: Double,
    val batteriaPercentuale: Int?,
    val autonomiaKm: Double?,
    val tariffaSbloccoEuro: Double,
    val tariffaAlMinutoEuro: Double,
    val disponibile: Boolean
) : Parcelable {

    /** Comodo nei layout: evita di ripetere il confronto */
    val isEbike: Boolean
        get() = tipo == VehicleType.EBIKE
}