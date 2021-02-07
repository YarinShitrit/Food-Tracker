package com.example.burgertracker.dagger

import android.app.Application
import com.example.burgertracker.AppRepository
import com.example.burgertracker.db.PlaceDao
import com.example.burgertracker.db.PlaceDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PlacesModule(private val application: Application) {

    @Provides
    @Singleton
    fun getPlacesRepo(): AppRepository = AppRepository()

    @Provides
    @Singleton
    fun getPlacesDB(): PlaceDatabase = PlaceDatabase.invoke(application)

    @Provides
    @Singleton
    fun getPlacesDao(db: PlaceDatabase): PlaceDao = db.getPlaceDao()

}