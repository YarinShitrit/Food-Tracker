package com.example.burgertracker.retrofit

import com.example.burgertracker.data.Result
import retrofit2.Call
import retrofit2.http.GET

import retrofit2.http.Query


interface PlacesRetrofitInterface {

    @GET("/maps/api/place/textsearch/json?")
    fun getNearbyPlaces(
        @Query("query") query: String,
        @Query("types") types: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") key: String
    ): Call<Result>

    @GET("/maps/api/place/textsearch/json?")
    fun getNearbyPlaces(
        @Query("types") types: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") key: String
    ): Call<Result>

    @GET("/maps/api/place/textsearch/json?")
    fun getNearbyPlaces(
        @Query("pagetoken") next_page_token: String,
        @Query("key") key: String
    ): Call<Result>


}