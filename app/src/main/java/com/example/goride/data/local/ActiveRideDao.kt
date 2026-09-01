package com.example.goride.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ActiveRideDao {

    @Query("SELECT * FROM corsa_attiva WHERE id = ${ActiveRideEntity.RIGA_UNICA}")
    suspend fun getCorsaAttiva(): ActiveRideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salva(corsa: ActiveRideEntity)

    @Query("DELETE FROM corsa_attiva")
    suspend fun cancella()
}