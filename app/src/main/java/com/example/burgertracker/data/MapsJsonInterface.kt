package com.example.burgertracker.data

import com.example.burgertracker.db.JsonResults
import com.example.burgertracker.db.PlaceEntity
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET

import retrofit2.http.Query


interface MapsJsonInterface {

    @GET("/maps/api/place/textsearch/json?")
    fun getNearbyPlaces(
        @Query("query") query: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") key: String
    ): Call<JsonResults>
}