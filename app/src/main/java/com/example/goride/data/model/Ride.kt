package com.example.goride.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Una corsa, in corso o conclusa (RF10, RF11).
 * endTimestamp resta null finche' la corsa e' attiva.
 */
@Parcelize
data class Ride(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicleNome: String,
    val vehicleTipo: VehicleType,
    val startTimestamp: Long,
    val endTimestamp: Long?,
    val minutiPrepagati: Int,
    val tariffaSbloccoEuro: Double,
    val tariffaAlMinutoEuro: Double,
    val costoTotaleEuro: Double
) : Parcelable {

    val isAttiva: Boolean
        get() = endTimestamp == null

    val durataSecondi: Long
        get() = if (endTimestamp != null) (endTimestamp - startTimestamp) / 1000 else 0
}
