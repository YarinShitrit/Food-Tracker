package com.example.burgertracker.map

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.burgertracker.AppRepository
import com.example.burgertracker.placesData.Place
import com.example.burgertracker.placesData.PlaceReview
import com.example.burgertracker.user.User
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "MapViewModel"

class MapViewModel(private val appRepository: AppRepository) : ViewModel() {
    lateinit var appKey: String
    var isMapAvailable = false // becomes true when onMapReady() is called
    val appMap = MutableLiveData<GoogleMap>()
    val currentUser = MutableLiveData<FirebaseUser?>()
    private lateinit var currentUserID: String
    val currentUserPhoto = MutableLiveData<Bitmap>()
    val currentFragment = MutableLiveData<String>()
    val currentFocusedPlace = MutableLiveData<Place>()
    val currentFocusedPlaceReviews = MutableLiveData<ArrayList<PlaceReview>>()
    val placesList = MutableLiveData<ArrayList<Place>>()
    val mediatorPlace = MutableLiveData<Place>()
    val queryIcon = MutableLiveData<String>()
    val userLocation = MutableLiveData<LatLng>()
    val favPlaces = MutableLiveData<ArrayList<Place>>()
    private val fcmToken = MutableLiveData<String>()

    init {
        placesList.value = arrayListOf()
    }

    private fun initFirebaseCloudMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            Log.d(TAG, "Received Firebase token -> ${task.result}")
            // Get new FCM registration token
            fcmToken.value = task.result
            appRepository.setUserFCMToken(currentUserID, fcmToken.value!!)
        })
    }

    private fun initUserFirebaseDBListener() {
        appRepository.firebaseDBRef.child("users").child(currentUserID).child("favorite_places")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(
                        TAG,
                        "onChildAdded() called -> Adding place to Room DB: ${snapshot.value}"
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        val place =
                            placesList.value!!.find { it.place_id == (snapshot.value as HashMap<*, *>)["place_id"] }
                        if (place != null) {
                            Log.d(TAG, "Adding $place")
                            appRepository.placesDao.insertPlace(place)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildChanged() called -> ${snapshot.value}")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.d(
                        TAG,
                        "onChildRemoved() called -> Removing place from Room DB: ${snapshot.value}"
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        val placeID = (snapshot.value as HashMap<*, *>)["place_id"] as String
                        appRepository.placesDao.deletePlace(placeID)
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildMoved() called")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onChildCancelled() called")
                }
            })
    }

    /**
     *Creates a Retrofit call to Google Places API to retrieve JSON data about nearby places
     *@param query String?- the query entered for specific type of nearby places or null for getting all types nearby places
     */
    fun getNearbyPlaces(query: String?, searchRadius : Int) {
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
                MarkerIconGenerator.setPlacesMarkerIcon(it, queryIcon.value)
                mediatorPlace.postValue(it)
                val allPlaces =
                    ArrayList<Place>().apply {
                        addAll(placesList.value!!)
                        add(it)
                    }
                placesList.postValue(allPlaces)
                delay(50)
                //Log.d(TAG, "Total places in placesList - ${placesList.value?.size}")
            }
        }
    }

    fun getAllPlacesByDistance() {
        Log.d(TAG, "getAllPlacesByDistance() called")
        viewModelScope.launch(Dispatchers.IO) {
            val places = appRepository.getAllPlacesByDistanceLocally()
            favPlaces.postValue(ArrayList(places))
        }
    }

    fun addPlaceToFavorites(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        placesList.value?.find { (it.place_id == place.place_id) }.apply { this?.isLiked = true }
        appRepository.addPlaceToFavorites(currentUserID, place)
    }

    fun removePlaceFromFavorites(place: Place) = viewModelScope.launch(Dispatchers.IO) {
        placesList.value?.find { (it.place_id == place.place_id) }
            .apply { this?.isLiked = false }
        //favPlaces.value?.remove(place)
        appRepository.deletePlaceFromFavorites(currentUserID, place.place_id)
    }

    suspend fun getIfPlaceIsFavorite(place: Place) = appRepository.getPlaceLocally(place)

    fun deleteAllPlaces() {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.deleteAllPlaces(currentUserID)
            favPlaces.postValue(ArrayList())
        }
    }

    fun deleteAllPlacesLocally() {
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.placesDao.deleteAllPlaces()
        }
    }

    private fun setPlacesDistance(place: Place) {
        place.apply {
            val placeLocation = Location("destination")
                .apply {
                    latitude = place.geometry.location.lat
                    longitude = place.geometry.location.lng
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
            place.distance = stringDistance.toFloat()
        }
    }

    fun initUserData(user: FirebaseUser, fbToken: String? = null) {
        currentUser.value = user
        currentUserID = currentUser.value!!.uid
        getUserFavoritePlaces()
        downloadCurrentUserPhoto(fbToken)
        initUserFirebaseDBListener()
        initFirebaseCloudMessaging()
    }

    private fun getUserFavoritePlaces() {
        Log.d(TAG, "getUserFavoritePlaces() called -> $currentUserID")
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.getUserFavoritePlaces(currentUserID)
        }
    }

    private fun downloadCurrentUserPhoto(fbToken: String? = null) {
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
        Log.d(TAG, "createNewUser() called")
        val firebaseUser = currentUser.value!!
        val user = User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.email, fbToken)
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.createNewUser(user)
        }
    }

    fun addUserToPlaceCloudUpdates() {
        Log.d(TAG, "addUserToPlaceCloudUpdates() called")
        appRepository.addUserToPlaceCloudUpdates(
            currentUserID,
            currentFocusedPlace.value!!.place_id
        )
    }

    fun removeUserFromPlaceCloudUpdates() {
        Log.d(TAG, "removeUserFromPlaceCloudUpdates() called")
        appRepository.removeUserFromPlaceCloudUpdates(
            currentUserID,
            currentFocusedPlace.value!!.place_id
        )
    }

    fun downloadDetailedPlaceReviews() {
        Log.d(TAG, "downloadDetailedPlaceReviews() called")
        viewModelScope.launch(Dispatchers.IO) {
            appRepository.getPlaceReviews(currentFocusedPlace.value!!.place_id, appKey).collect {
                Log.d(TAG, "Received Reviews - $it")
                currentFocusedPlaceReviews.postValue(it)
            }
        }
    }
}