package com.example.goride.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * DAO delle corse (Lez. 2.9).
 *
 * I metodi che restituiscono LiveData non sono suspend: Room aggiorna
 * la LiveData da solo su thread secondario a ogni cambiamento della
 * tabella.
 */
@Dao
interface RideDao {

    /** RF10 - storico dell'utente, dalla piu' recente */
    @Query("SELECT * FROM corse WHERE userId = :userId ORDER BY startTimestamp DESC")
    fun getCorseUtente(userId: String): LiveData<List<RideEntity>>

    @Query("SELECT * FROM corse WHERE id = :id")
    suspend fun getCorsaById(id: String): RideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisci(corsa: RideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisciTutte(corse: List<RideEntity>)

    @Query("DELETE FROM corse WHERE userId = :userId")
    suspend fun cancellaCorseUtente(userId: String)
}