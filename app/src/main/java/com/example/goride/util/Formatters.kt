package com.example.goride.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Formattazioni condivise, per non ripetere la stessa logica nei ViewModel. */
object Formatters {

    /** 3725 secondi -> "1h 02m" ; 125 -> "2m 05s" */
    fun durata(secondi: Long): String {
        val ore = secondi / 3600
        val minuti = (secondi % 3600) / 60
        val sec = secondi % 60

        return when {
            ore > 0 -> String.format(Locale.ITALY, "%dh %02dm", ore, minuti)
            else -> String.format(Locale.ITALY, "%dm %02ds", minuti, sec)
        }
    }

    /** 4.5 -> "4,50 €" */
    fun euro(importo: Double): String =
        String.format(Locale.ITALY, "%.2f €", importo)

    /** timestamp -> "12 marzo 2026, 14:30" */
    fun dataOra(timestamp: Long): String {
        val formato = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.ITALY)
        return formato.format(Date(timestamp))
    }

    /** timestamp -> "12/03/2026" */
    fun dataBreve(timestamp: Long): String {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        return formato.format(Date(timestamp))
    }
}