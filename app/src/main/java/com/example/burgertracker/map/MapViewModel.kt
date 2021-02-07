package com.example.burgertracker.map

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burgertracker.AppRepository
import com.example.burgertracker.R
import com.example.burgertracker.placesData.Place
import com.example.burgertracker.user.User
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Singleton


private const val TAG = "MapViewModel"

@Singleton
class MapViewModel(private val appRepository: AppRepository) : ViewModel() {
    lateinit var appKey: String
    var isMapAvailable = false // becomes true when onMapReady() is called
    val appMap = MutableLiveData<GoogleMap>()
    val currentUser = MutableLiveData<FirebaseUser?>()
    val currentUserPhoto = MutableLiveData<Bitmap>()
    val currentFragment = MutableLiveData<String>()
    val currentFocusedPlace = MutableLiveData<Place>()
    val placesList = MutableLiveData<ArrayList<Place>>()
    val mediator = MediatorLiveData<ArrayList<Place>>()
    val queryIcon = MutableLiveData<String>()
    val userLocation = MutableLiveData<LatLng>()
    private val searchRadius = 5

    init {
        placesList.value = arrayListOf()
        mediator.addSource(appRepository.placesList) {
            Log.d(TAG, "places retrieved from repo")
            mediator.value = it
            it.addAll(placesList.value!!)
            placesList.value = it
            Log.d(TAG, "placesList size is ${placesList.value!!.size}")
        }
    }

    /**
     *Creates a Retrofit call to Google Places API to retrieve JSON data about nearby places
     *@param query String?- the query entered for specific type of nearby places or null for getting all types nearby places
     */
    fun getNearbyPlaces(query: String?) {
        Log.d(TAG, "getNearbyPlaces called")
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.getNearbyPlaces(
                query,
                "restaurant",
                userLocation.value!!,
                appKey,
                searchRadius * 1000
            )
        }
    }

    fun addPlaceToFavorites(place: Place) {
        Log.d(TAG, "addPlaceToFavorites() called")
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.addPlaceToFavorites(
                currentUser.value!!.uid,
                place
            )
            Log.d(TAG, "place inserted")
            val places = appRepository.getAllPlaces()
            Log.d(
                TAG,
                "Added ${place.name} to favorites. \nPlaces in favorites are $places"
            )
        }
    }

    fun deletePlace(place: Place) =
        viewModelScope.launch(Dispatchers.IO) { appRepository.deletePlace(place) }

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

    fun downloadCurrentUserPhoto(fbToken: String? = null) {
        Log.d(
            TAG,
            "downloadCurrentUserPhoto called -> downloading photo from ${currentUser.value?.photoUrl.toString()}"
        )
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val photo = async {
                    appRepository.downloadUserPhoto(currentUser.value!!, fbToken)
                }
                currentUserPhoto.postValue(photo.await())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download user photo , ${e.printStackTrace()}")
        }
    }

    fun createNewUser(fbToken: String? = null) {
        val firebaseUser = currentUser.value!!
        val user = User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email, fbToken)
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.createNewUser(user)
        }
    }
}