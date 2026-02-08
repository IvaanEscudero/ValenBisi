package com.example.valenbisi

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*
* Defines the database for the application using Room
 */
@Database(entities = [IncidentReport::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentReportDao(): IncidentReportDao

    // Singleton pattern
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null // Holds the singleton instance of the database

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // If INSTANCE is null, create a new database instance
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,    // Database class
                    "valenbisi_database" // Name of the database file
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}