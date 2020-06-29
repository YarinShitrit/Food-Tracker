package com.example.burgertracker.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PlaceEntity::class], version = 1)
abstract class PlaceDataBase : RoomDatabase() {

    abstract fun getPlaceDao(): PlaceDao

    companion object {
        @Volatile
        private var instance: PlaceDataBase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, PlaceDataBase::class.java, "place_database").build()
    }
}