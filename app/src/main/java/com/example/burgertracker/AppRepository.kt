package com.example.burgertracker

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.db.PlaceDao
import com.example.burgertracker.placesData.Place
import com.example.burgertracker.placesData.PlaceResult
import com.example.burgertracker.retrofit.PlacesRetrofitInterface
import com.example.burgertracker.user.User
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
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

    suspend fun getNearbyPlaces(
        query: String?,
        type: String,
        location: LatLng,
        key: String,
        searchRadius: Int
    ) {
        Log.d(TAG, "getNearbyPlaces called")
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
                getNearbyPlacesWithToken(key, retrofitResponse.body()?.next_page_token!!)//Recursion
            }
        }
    }

    fun createNewUser(user: User) {
        Log.d(TAG, "createNewUser() called")
        firebaseDBRef.child("users").child(user.id).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d(TAG, "User successfully created ")
            } else {
                Log.d(TAG, "Failed to create user to database")
            }
        }
    }

    suspend fun addPlaceToFavorites(userID: String, place: Place) {
        Log.d(TAG, "addPlaceToFavorites() called")
        placesDao.insertPlace(place)
        firebaseDBRef.child("users").child(userID).child("favorite_places").child(place.place_id)
            .setValue(place)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d(TAG, "Successfully added place to firebase database")
                } else {
                    Log.d(TAG, "Failed to add place to firebase database")
                }
            }
    }

    suspend fun deletePlace(place: Place) = placesDao.deletePlace(place)
    suspend fun getPlace(place: Place) = placesDao.getIfPlaceIsFavorite(place.place_id)
    suspend fun getAllPlaces() = placesDao.getAllPlacesAsync()
    suspend fun deleteAllPlaces() = placesDao.deleteAllPlaces()

}