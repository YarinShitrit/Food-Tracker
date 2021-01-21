package com.example.burgertracker.dagger

import com.example.burgertracker.retrofit.PlacesRetrofitInterface
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
}