package com.example.burgertracker.dagger

import com.example.burgertracker.retrofit.PlacesRetrofitInterface
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
class NetworkModule {
    companion object {
        private const val BASE_URL = "https://maps.googleapis.com"
    }

    @Provides
    @Singleton
    fun provideRetrofitService(): PlacesRetrofitInterface {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(PlacesRetrofitInterface::class.java)
    }

    @Provides
    @Singleton
    fun getFirebaseRealTimeDBRef() = Firebase.database.reference
}