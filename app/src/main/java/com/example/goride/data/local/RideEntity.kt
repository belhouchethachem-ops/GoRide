package com.example.goride.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.goride.data.model.Ride
import com.example.goride.data.model.VehicleType

/**
 * Cache locale delle corse concluse (Lez. 2.9).
 *
 * Room non conosce i nostri modelli di dominio, e va bene cosi':
 * l'entity e' un dettaglio del layer dati. Le funzioni di conversione
 * qui sotto tengono i due mondi separati.
 */
@Entity(tableName = "corse")
data class RideEntity(

    @PrimaryKey
    val id: String,

    val userId: String,
    val vehicleId: String,
    val vehicleNome: String,
    val vehicleTipo: String,      // enum salvato come String: Room non gestisce enum nativamente
    val startTimestamp: Long,
    val endTimestamp: Long?,
    val minutiPrepagati: Int,
    val tariffaSbloccoEuro: Double,
    val tariffaAlMinutoEuro: Double,
    val costoTotaleEuro: Double
) {

    /** Entity -> dominio */
    fun toRide(): Ride = Ride(
        id = id,
        userId = userId,
        vehicleId = vehicleId,
        vehicleNome = vehicleNome,
        vehicleTipo = VehicleType.valueOf(vehicleTipo),
        startTimestamp = startTimestamp,
        endTimestamp = endTimestamp,
        minutiPrepagati = minutiPrepagati,
        tariffaSbloccoEuro = tariffaSbloccoEuro,
        tariffaAlMinutoEuro = tariffaAlMinutoEuro,
        costoTotaleEuro = costoTotaleEuro
    )

    companion object {
        /** Dominio -> entity */
        fun fromRide(ride: Ride): RideEntity = RideEntity(
            id = ride.id,
            userId = ride.userId,
            vehicleId = ride.vehicleId,
            vehicleNome = ride.vehicleNome,
            vehicleTipo = ride.vehicleTipo.name,
            startTimestamp = ride.startTimestamp,
            endTimestamp = ride.endTimestamp,
            minutiPrepagati = ride.minutiPrepagati,
            tariffaSbloccoEuro = ride.tariffaSbloccoEuro,
            tariffaAlMinutoEuro = ride.tariffaAlMinutoEuro,
            costoTotaleEuro = ride.costoTotaleEuro
        )
    }
}