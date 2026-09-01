package com.example.goride.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Database dell'app (Lez. 2.9).
 *
 * Singleton con @Volatile + synchronized: aprire piu' istanze dello
 * stesso database e' costoso e puo' portare a stati incoerenti.
 *
 * @Volatile garantisce che il valore di INSTANCE sia visibile a tutti
 * i thread immediatamente; synchronized impedisce che due thread
 * creino l'istanza contemporaneamente.
 */
@Database(
    entities = [RideEntity::class, ActiveRideEntity::class],
    version = 1,
    exportSchema = true
)
abstract class GoRideDatabase : RoomDatabase() {

    abstract fun rideDao(): RideDao
    abstract fun activeRideDao(): ActiveRideDao

    companion object {

        @Volatile
        private var INSTANCE: GoRideDatabase? = null

        fun getDatabase(context: Context): GoRideDatabase {
            return INSTANCE ?: synchronized(this) {
                val istanza = Room.databaseBuilder(
                    context.applicationContext,   // applicationContext: mai quello dell'Activity
                    GoRideDatabase::class.java,
                    NOME_DATABASE
                )
                    // In sviluppo va bene: a ogni cambio di schema il DB
                    // viene ricreato. In produzione servirebbero le Migration.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = istanza
                istanza
            }
        }

        private const val NOME_DATABASE = "goride_database"
    }
}