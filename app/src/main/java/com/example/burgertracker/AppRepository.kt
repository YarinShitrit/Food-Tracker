package com.example.burgertracker

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.burgertracker.data.Place
import com.example.burgertracker.data.PlaceResult
import com.example.burgertracker.retrofit.PlacesRetrofitInterface
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://maps.googleapis.com"
private const val TAG = "AppRepository"

class AppRepository {
    val placesList = MutableLiveData<ArrayList<Place>>()

    suspend fun getNearbyPlaces(
        query: String?,
        type: String,
        location: LatLng,
        key: String,
        searchRadius: Int
    ) {
        Log.d(TAG, "getNearbyPlaces called")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val placesInterface = retrofit.create(PlacesRetrofitInterface::class.java)
        val retrofitResponse: Response<PlaceResult>
        if (!query.isNullOrEmpty()) {
            retrofitResponse = placesInterface.getNearbyPlaces(
                query.toString(),
                type,
                "${location.latitude}, ${location.longitude}",
                searchRadius,
                key
            )
        } else {
            retrofitResponse = placesInterface.getNearbyPlaces(
                type,
                "${location.latitude}, ${location.longitude}",
                searchRadius,
                key
            )
        }
        Log.d(TAG, "API CALL URL -> ${retrofitResponse.raw().request().url()}")
        if (retrofitResponse.isSuccessful) {
            Log.d(TAG, "SUCCESS ${retrofitResponse.body()?.results!!}")
            placesList.postValue(retrofitResponse.body()?.results!!)
            if (!retrofitResponse.body()?.next_page_token.isNullOrEmpty()) {
                delay(1750)
                getNearbyPlacesWithToken(key, retrofitResponse.body()?.next_page_token!!)
            }
        }
    }

    private suspend fun getNearbyPlacesWithToken(key: String, nextPageToken: String) {
        Log.d(TAG, "getNearbyPlacesWithToken called")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val placesInterface = retrofit.create(PlacesRetrofitInterface::class.java)
        val retrofitResponse: Response<PlaceResult>
        retrofitResponse = placesInterface.getNearbyPlaces(
            nextPageToken,
            key
        )
        if (retrofitResponse.isSuccessful) {
            placesList.postValue((retrofitResponse.body()?.results!!))
            if (!retrofitResponse.body()?.next_page_token.isNullOrEmpty()) {
                delay(1750)
                getNearbyPlacesWithToken(key, retrofitResponse.body()?.next_page_token!!)
            }
        }
    }
}