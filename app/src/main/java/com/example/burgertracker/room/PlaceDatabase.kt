package com.example.burgertracker.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.burgertracker.placesData.Place

@Database(entities = [Place::class], version = 1, exportSchema = false)
abstract class PlaceDatabase : RoomDatabase() {

    abstract fun getPlaceDao(): PlaceDao

    companion object {
        @Volatile
        private var instance: PlaceDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, PlaceDatabase::class.java, "place_database")
                .fallbackToDestructiveMigration().build()
    }
}