package com.example.burgertracker

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.placesData.Place
import com.example.burgertracker.placesData.PlaceResult
import com.example.burgertracker.retrofit.PlacesRetrofitInterface
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AppRepository"

@Singleton
class AppRepository {

    init {
        Injector.applicationComponent.inject(this)
    }

    val placesList = MutableLiveData<ArrayList<Place>>()

    @Inject
    lateinit var retrofit: PlacesRetrofitInterface

    suspend fun getNearbyPlaces(
        query: String?,
        type: String,
        location: LatLng,
        key: String,
        searchRadius: Int
    ) {
        Log.d(TAG, "getNearbyPlaces called")
        //val placesInterface = retrofit.create(PlacesRetrofitInterface::class.java)
        val retrofitResponse: Response<PlaceResult>
        if (!query.isNullOrEmpty()) {
            retrofitResponse = retrofit.getNearbyPlaces(
                query.toString(),
                type,
                "${location.latitude}, ${location.longitude}",
                searchRadius,
                key
            )
        } else {
            retrofitResponse = retrofit.getNearbyPlaces(
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
        val retrofitResponse: Response<PlaceResult> = retrofit.getNearbyPlaces(
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
/*
    suspend fun insertPlace(place: Place) = placesDao.insertPlace(place)
    suspend fun deletePlace(place: Place) = placesDao.deletePlace(place)
    suspend fun getPlace(place: Place) = placesDao.getIfPlaceIsFavorite(place.place_id)
    suspend fun getAllPlaces() = placesDao.getAllPlaces()
    suspend fun deleteAllPlaces() = placesDao.deleteAllPlaces()
*/
}