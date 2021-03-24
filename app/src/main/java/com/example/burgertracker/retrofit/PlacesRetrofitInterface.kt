package com.example.burgertracker.retrofit

import com.example.burgertracker.placesData.PlaceResult
import com.example.burgertracker.placesData.PlaceReview
import com.example.burgertracker.placesData.PlaceReviewResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface PlacesRetrofitInterface {
    /**
     * Call Places API and get nearby places in JSON format
     * @param query String - The query search
     * @param types String - The type of places to search e.g restaurant, airport, etc..
     * @param location String - the LatLng of the location displayed like the following - latitude , longitude
     * @param radius Int - The radius for searching places in meters
     * @param key String - The Places API key
     */
    @GET("/maps/api/place/textsearch/json?")
    suspend fun getNearbyPlaces(
        @Query("query") query: String,
        @Query("type") types: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") key: String
    ): Response<PlaceResult>

    /**
     * Call Places API and get nearby places in JSON format without query
     * @param types String - The type of places to search e.g restaurant, airport, etc..
     * @param location String - the LatLng of the location displayed like the following - latitude , longitude
     * @param radius Int - The radius for searching places in meters
     * @param key String - The Places API key
     */
    @GET("/maps/api/place/nearbysearch/json?")
    suspend fun getNearbyPlaces(
        @Query("type") types: String,
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("key") key: String
    ): Response<PlaceResult>

    /**
     * Call Places API and get nearby places in JSON format from next page token
     * @param next_page_token String - The token received from previous API call that indicates the next page of places available
     * @param key String - The Places API key
     */
    @GET("/maps/api/place/nearbysearch/json?")
    suspend fun getNearbyPlaces(
        @Query("pagetoken") next_page_token: String,
        @Query("key") key: String
    ): Response<PlaceResult>

    @GET("/maps/api/place/details/json?fields=review")
    suspend fun getPlaceReviews(
        @Query("place_id") placeId: String,
        @Query("key") key: String,
    ): Response<PlaceReviewResult>


}