package com.example.burgertracker.map


import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.example.burgertracker.AppRepository
import com.example.burgertracker.R
import com.example.burgertracker.data.Place
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlin.collections.ArrayList


private const val TAG = "MapViewModel"

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val appKey = application.resources.getString(R.string.google_maps_key)
    private val appRepository = AppRepository()
    val placesList = MutableLiveData<ArrayList<Place>>()
    val mediator = MediatorLiveData<ArrayList<Place>>()
    val queryIcon = MutableLiveData<String>()
    val userLocation = MutableLiveData<LatLng>()
    val searchRadius = MutableLiveData<Int>(
        application.getSharedPreferences("prefs", Context.MODE_PRIVATE).getInt("radius", 15)
    )

    init {
        placesList.value = arrayListOf()
        mediator.addSource(appRepository.placesList) {
            Log.d(TAG, "places retrieved from repo")
            mediator.value = it
            it.addAll(placesList.value!!)
            placesList.value = it
            Log.d(TAG, "placesList size is ${placesList.value!!.size}")
            //addPlaces(it)
        }
    }

    /**
     *Shoots a Retrofit call to Google Places API to retrieve JSON data about nearby places
     *@param query String?- the query entered for specific type of nearby places or null for getting all types nearby places
     *@param nextPageToken String?- The token indicating the next page available form the API call for more places to retrieve
     */
    suspend fun getNearbyPlaces(query: String?) {
        Log.d(TAG, "getNearbyPlaces called")
        appRepository.getNearbyPlaces(
            query,
            "restaurant",
            userLocation.value!!,
            appKey,
            searchRadius.value!! * 1000
        )
    }

    fun setPlacesMarkerIcon(list: ArrayList<Place>) {
        if (!queryIcon.value.isNullOrEmpty()) {
            list.forEach {
                when (queryIcon.value) {
                    "Pizza" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.pizza)
                    "Burger" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
                    "Sushi" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.sushi)
                    "Mexican" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.taco)
                    "Coffee" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.cafe)
                }
            }
        }
        list.forEach {
            val place = it
            if (place.markerIcon == null) {
                if (place.name.contains("pizza".trim(), true) || place.name.contains(
                        "pizzeria".trim(),
                        true
                    ) || place.name.contains("פיצה".trim(), true)
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.pizza)
                } else if (place.name.contains("burger".trim(), true) || place.name.contains(
                        "בורגר".trim(),
                        true
                    )
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
                } else if (place.name.contains("sushi", true) || place.name.contains(
                        "סושי",
                        true
                    )
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.sushi)
                } else if (place.name.contains("cafe".trim(), true) || place.name.contains(
                        "קפה".trim(),
                        true
                    )
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.cafe)
                } else if (place.name.contains("taco".trim(), true)) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.taco)
                }
            }
        }
    }

    fun setPlacesDistance(list: ArrayList<Place>) {
        list.forEach {
            val placeLocation = Location("destination")
                .apply {
                    latitude = it.geometry.location.lat
                    longitude = it.geometry.location.lng
                }
            val currentUserLocation = Location("current location")
                .apply {
                    latitude = userLocation.value?.latitude!!
                    longitude = userLocation.value?.longitude!!
                }
            val distance =
                currentUserLocation.distanceTo(placeLocation) / 1000 // from meter to kilometer
            var stringDistance = distance.toString()
            stringDistance =
                stringDistance.substring(0, stringDistance.indexOf(".") + 2)
            it.distance = stringDistance.toFloat()
        }
    }
}
