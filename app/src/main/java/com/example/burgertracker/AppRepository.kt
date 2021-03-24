package com.example.burgertracker

import android.graphics.Bitmap
import android.util.Log
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.db.PlaceDao
import com.example.burgertracker.placesData.Place
import com.example.burgertracker.placesData.PlaceResult
import com.example.burgertracker.placesData.PlaceReview
import com.example.burgertracker.placesData.PlaceReviewResult
import com.example.burgertracker.retrofit.PlacesRetrofitInterface
import com.example.burgertracker.user.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

private const val TAG = "AppRepository"

class AppRepository {
    init {
        Injector.applicationComponent.inject(this)
    }

    @Inject
    lateinit var retrofit: PlacesRetrofitInterface

    @Inject
    lateinit var firebaseDBRef: DatabaseReference

    @Inject
    lateinit var placesDao: PlaceDao

    fun downloadUserPhoto(currentUser: FirebaseUser, fbToken: String?): Bitmap {
        return if (fbToken.isNullOrEmpty()) {
            Picasso.get().load(currentUser.photoUrl).resize(200, 200).centerInside().get()
        } else {
            Picasso.get().load(currentUser.photoUrl.toString() + "?access_token=$fbToken")
                .resize(200, 200).get()
        }
    }

    suspend fun getPlaceReviews(placeID: String, key: String): Flow<ArrayList<PlaceReview>?> =
        flow {
            Log.d(TAG, "getPlaceReviews() called ")
            try {
                val retrofitResponse: Response<PlaceReviewResult> = retrofit.getPlaceReviews(
                    placeID,
                    key
                )
                if (retrofitResponse.isSuccessful) {
                    Log.d(
                        TAG,
                        "Reviews - ${retrofitResponse.body()} \n raw ${retrofitResponse.raw()}"
                    )
                    emit(retrofitResponse.body()?.result?.reviews)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get place Reviews -> ${e.localizedMessage}")
            }
        }

    suspend fun getNearbyPlaces(
        query: String?,
        type: String,
        location: LatLng,
        key: String,
        searchRadius: Int,
    ): Flow<Place> = flow {
        Log.d(TAG, "getNearbyPlaces called")
        try {
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
                Log.d(TAG, "emit from nearbyPlaces")
                retrofitResponse.body()?.results!!.forEach { emit(it) }
                if (!retrofitResponse.body()?.next_page_token.isNullOrEmpty()) {
                    Log.d(TAG, "next page available")
                    delay(1750)
                    getNearbyPlacesWithToken(
                        key,
                        retrofitResponse.body()?.next_page_token!!
                    ).collect {
                        emit(it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "failed to get places, ${e.localizedMessage}")
            Log.e(TAG, "${e.printStackTrace()}")
        }
    }

    private suspend fun getNearbyPlacesWithToken(
        key: String,
        nextPageToken: String
    ): Flow<Place> = flow {
        Log.d(TAG, "getNearbyPlacesWithToken called")
        val retrofitResponse: Response<PlaceResult> = retrofit.getNearbyPlaces(
            nextPageToken,
            key
        )
        if (retrofitResponse.isSuccessful) {
            Log.d(TAG, "emit from nearbyPlacesWithToken")
            retrofitResponse.body()?.results!!.forEach { emit(it) }
            (retrofitResponse.body()?.results!!)
            if (!retrofitResponse.body()?.next_page_token.isNullOrEmpty()) {
                Log.d(
                    TAG,
                    "token available - URL CALL IS ${retrofitResponse.raw().request().url()}"
                )
                delay(1750)
                getNearbyPlacesWithToken(
                    key,
                    retrofitResponse.body()?.next_page_token!!
                ).collect {
                    emit(it)
                }
            } else {
                Log.d(TAG, "token is ${retrofitResponse.body()?.next_page_token} ")
            }
        }
    }

    fun createNewUser(user: User) {
        Log.d(TAG, "createNewUser() called -> user is $user")
        firebaseDBRef.child("users").child(user.id).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "User successfully created ")
            } else {
                Log.d(TAG, "Failed to create user to database")
            }
        }
    }

    /**
     * Adding a place to user favorites and afterwards increasing the place total favorites by 1
     */
    fun addPlaceToFavorites(userID: String, place: Place) {
        Log.d(TAG, "addPlaceToFavorites() called")
        firebaseDBRef.child("users").child(userID).child("favorite_places").child(place.place_id)
            .setValue(place)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Successfully added ${place.name} to firebase database")
                } else {
                    Log.d(TAG, "Failed to add place to firebase database")
                }
            }
    }

    fun deletePlaceFromFavorites(userID: String, placeID: String) {
        Log.d(TAG, "removePlaceFromFavorites() called")
        firebaseDBRef.child("users").child(userID).child("favorite_places").child(placeID)
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Successfully removed place from firebase database")
                } else {
                    Log.d(TAG, "Failed to remove place from firebase database")
                }
            }
    }

    fun addUserToPlaceCloudUpdates(userID: String, placeID: String) {
        //Adding user as a subscriber to the place in the database location
        firebaseDBRef.child("places").child(placeID).child("subscribers").child(userID)
            .setValue(userID).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Added user $userID as subscriber to place $placeID")
                } else {
                    Log.d(TAG, "Failed to add user $userID as subscriber to place $placeID")
                }
            }
        //Adding the user to FCM place topic
        Firebase.messaging.subscribeToTopic(placeID)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Added user $userID to $placeID FCM updates")
                } else {
                    Log.d(TAG, "Failed to add user $userID to $placeID FCM updates")
                }
            }
    }

    fun removeUserFromPlaceCloudUpdates(userID: String, placeID: String) {
        //Unsubscribe the user from the place in the database location
        firebaseDBRef.child("places").child(placeID).child("subscribers").child(userID)
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Unsubscribed user $userID from place $placeID")
                } else {
                    Log.d(TAG, "Failed to unsubscribe $userID from place $placeID")
                }
            }
        //Removing the user from FCM place topic
        Firebase.messaging.unsubscribeFromTopic(placeID)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Removed user $userID from $placeID FCM updates")
                } else {
                    Log.d(TAG, "Failed to remove user $userID from $placeID FCM updates")
                }
            }
    }

    suspend fun getPlaceLocally(place: Place) = placesDao.getIfPlaceIsFavorite(place.place_id)
    suspend fun getAllPlacesLocally() = placesDao.getAllPlacesAsync()
    suspend fun getAllPlacesByDistanceLocally() = placesDao.getAllPlacesByDistance()
    suspend fun deleteAllPlaces(userID: String) {
        Log.d(TAG, "deleteAllPlaces() called")
        firebaseDBRef.child("users").child(userID).child("favorite_places")
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Successfully removed all places from firebase database")
                } else {
                    Log.d(TAG, "Failed to remove all places from firebase database")
                }
            }
        placesDao.deleteAllPlaces()
    }

    fun setUserFCMToken(userID: String, token: String) {
        firebaseDBRef.child("users").child(userID).child("token").setValue(token)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Successfully assigned token $token to user $userID")
                } else {
                    Log.d(TAG, "Failed to assign token $token} to user $userID")
                }
            }
    }

    fun getUserFavoritePlaces(userID: String) {
        Log.d(TAG, "getUserFavoritePlaces() called")
        firebaseDBRef.child("users").child(userID).child("favorite_places").get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "User favorite places retrieved -> ${it.result.value}")
                    val places = it.result.value as Map<*, *>?
                    places?.values?.forEach { place ->
                        val jsonPlace = Gson().toJson(place)
                        Log.d(TAG, "place is $jsonPlace")
                        CoroutineScope(Dispatchers.IO).launch {
                            placesDao.insertPlace(Gson().fromJson(jsonPlace, Place::class.java))
                        }
                    }
                } else {
                    Log.d(TAG, "Failed to retrieve user favorite places")
                }
            }
    }
}