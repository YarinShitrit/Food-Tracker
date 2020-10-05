package com.example.burgertracker.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.google.android.libraries.places.api.model.Place

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlace(place: Place)

    @Query("SELECT * FROM placeentity")
    fun getAllPlaces(): LiveData<ArrayList<Place>>

    @Query("SELECT * FROM place WHERE ")
    fun getFavoritePlaces(): LiveData<ArrayList<Place>>

    @Query("DELETE FROM placeentity")
    fun deletePlace(placeName: Place)

    @Query("DELETE FROM placeentity")
    fun deleteAllPlaces()
}