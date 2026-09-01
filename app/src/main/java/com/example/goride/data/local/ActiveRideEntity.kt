package com.example.goride.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Corsa attualmente in corso.
 *
 * Serve a un solo scopo: se il sistema uccide il processo durante una
 * corsa, al riavvio ripristiniamo il timer da scadenzaTimestamp.
 *
 * La tabella contiene al massimo una riga: la chiave primaria e'
 * fissa a 1.
 */
@Entity(tableName = "corsa_attiva")
data class ActiveRideEntity(

    @PrimaryKey
    val id: Int = RIGA_UNICA,

    val vehicleId: String,
    val vehicleNome: String,
    val vehicleTipo: String,
    val startTimestamp: Long,
    val scadenzaTimestamp: Long,
    val minutiPrepagati: Int,
    val tariffaSbloccoEuro: Double,
    val tariffaAlMinutoEuro: Double
) {
    companion object {
        const val RIGA_UNICA = 1
    }
}