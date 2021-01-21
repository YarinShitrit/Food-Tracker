package com.example.burgertracker.dagger

import com.example.burgertracker.AppRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PlacesModule {

    @Provides
    @Singleton
    fun getPlacesRepo(): AppRepository = AppRepository()

}