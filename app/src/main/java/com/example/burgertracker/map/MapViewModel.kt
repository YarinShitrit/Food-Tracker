package com.example.burgertracker.map


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.burgertracker.AppUtils
import com.example.burgertracker.R
import com.example.burgertracker.data.Place
import com.example.burgertracker.retrofit.PlacesRetrofitInterface
import com.example.burgertracker.data.Result
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


private const val TAG = "MapViewModel"
private const val BASE_URL = "https://maps.googleapis.com"


class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val appKey = application.resources.getString(R.string.google_maps_key)
    val placesList = MutableLiveData<ArrayList<Place>>()
    val userLocation = MutableLiveData<LatLng>()

    init {
        placesList.value = arrayListOf()
    }

    /**
     *Shoots a Retrofit call to Google Places API to retrieve JSON data about nearby places
     *@param query String?- the query entered for specific type of nearby places or null for getting all types nearby places
     *@param nextPageToken String? - The token indicating the next page available form the API call for more places to retrieve
     */
    fun getNearbyPlaces(query: String?, nextPageToken: String? = null) {
        Log.d(TAG, "getNearbyPlaces called")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val placesInterface = retrofit.create(PlacesRetrofitInterface::class.java)
        val retrofitCall: Call<Result>
        if (!nextPageToken.isNullOrEmpty()) {
            Log.d(TAG, "Calling with token $nextPageToken")
            retrofitCall = placesInterface.getNearbyPlaces(
                nextPageToken,
                appKey

            )
        } else {
            if (!query.isNullOrEmpty()) {
                retrofitCall = placesInterface.getNearbyPlaces(
                    query.toString(),
                    "restaurant",
                    "${userLocation.value?.latitude}, ${userLocation.value?.longitude}",
                    1000,
                    appKey
                )
            } else {
                retrofitCall = placesInterface.getNearbyPlaces(
                    "restaurant",
                    "${userLocation.value?.latitude}, ${userLocation.value?.longitude}",
                    1000,
                    appKey
                )
            }
        }
        val coroutineJob = CoroutineScope(Dispatchers.IO).launch {
            if (!placesList.value.isNullOrEmpty()) {
                delay(1750)
            }
            retrofitCall.enqueue(object : Callback<Result> {
                override fun onFailure(p0: Call<Result>, p1: Throwable) {
                    Log.e(TAG, "failed" + p1.localizedMessage)
                }

                override fun onResponse(
                    call: Call<Result>,
                    response: Response<Result>
                ) {
                    if (response.isSuccessful) {
                        Log.d(
                            TAG,
                            "Success Retrofit call, status is ${response.body()?.status}\n${
                                response.body().toString()
                            }"
                        )
                        val placesArrayList = ArrayList<Place>().apply {
                            addAll(placesList.value!!)
                            addAll(response.body()?.results!!)
                        }
                        placesList.value = placesArrayList
                        AppUtils().setPlaceMarkerIcon(
                            placesList.value!!,
                            query?.trim()?.toLowerCase()
                        )
                        Log.d(TAG, "placesList size is ${placesList.value?.size}")
                        if (!response.body()?.next_page_token.isNullOrEmpty() && query.isNullOrEmpty()) {
                            val token = response.body()?.next_page_token
                            Log.d(TAG, "Token available - $token, calling getNearbyPlaces() ")
                            getNearbyPlaces(query.toString(), token)
                        }

                    } else {
                        Log.e(TAG, "Failed Retrofit call, ${response.message()}")
                    }
                }
            })
        }

    }

    private fun getSpecificPlace(requestedPlace: Place) {
        Log.d(TAG, "getSpecificPlace called")
        /*/** checks if the place is already in the viewModel data
         * so just send the place instead of performing an api request*/
        if (requestedPlace.formatted_phone_number == null || requestedPlace.photo_reference == null) {
            placeJsonString = ""
            val searchURL =
                ("https://maps.googleapis.com/maps/api/place/details/json?placeid=${requestedPlace.place_id}&key=${AppUtils().getKey()}")
            val pictureSearchURL =
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${requestedPlace.imageString}&key=${AppUtils().getKey()}"
            Log.d(TAG, "Place request - $searchURL")
            Log.d(TAG, "Photo request - $pictureSearchURL")
            jsonHandler.getJsonStringResult(pictureSearchURL, requestedPlace, true)
            jsonHandler.getJsonStringResult(searchURL, requestedPlace)
        }

         */
    }


}