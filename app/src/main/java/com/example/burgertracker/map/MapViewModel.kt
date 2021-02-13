package com.example.burgertracker.map

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burgertracker.AppRepository
import com.example.burgertracker.placesData.Place
import com.example.burgertracker.user.User
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
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
    val mediatorPlacesList = MutableLiveData<ArrayList<Place>>()
    val queryIcon = MutableLiveData<String>()
    val userLocation = MutableLiveData<LatLng>()
    private val searchRadius = 5

    init {
        placesList.value = arrayListOf()
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
            ).collect {
                setPlacesDistance(it)
                MarkerIconGenerator.setPlacesMarkerIcon(it,queryIcon.value)
                mediatorPlacesList.postValue(it)
                val allPlaces =
                    ArrayList<Place>().apply {
                        addAll(placesList.value!!)
                        addAll(it)
                    }
                Log.d(TAG, "ALLPLACES- $allPlaces")
                placesList.postValue(allPlaces)
                Log.d(TAG, "Total places in placesList - ${placesList.value?.size}")
            }
        }
    }

    fun addPlaceToFavorites(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        placesList.value?.find { (it.place_id == place.place_id) }.apply { this?.isLiked = true }
        appRepository.addPlaceToFavorites(currentUser.value!!.uid, place)
    }

    fun removePlaceFromFavorites(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        placesList.value?.find { (it.place_id == place.place_id) }
            .apply { this?.isLiked = false }
        appRepository.removePlaceFromFavorites(currentUser.value!!.uid, place)
    }

    suspend fun getIfPlaceIsFavorite(place: Place) = appRepository.getPlace(place)

    fun deleteAllPlaces() =
        viewModelScope.launch(Dispatchers.IO) { appRepository.deleteAllPlaces() }


    private fun setPlacesDistance(list: ArrayList<Place>) {
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