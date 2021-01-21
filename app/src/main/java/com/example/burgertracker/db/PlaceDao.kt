package com.example.burgertracker.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.burgertracker.placesData.Place


@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlace(place: Place)

    @Query("SELECT * FROM places")
    fun getAllPlaces(): LiveData<ArrayList<Place>>

    @Query("SELECT * FROM places WHERE place_id = :placeID")
    fun getIfPlaceIsFavorite(placeID: String): Place

    @Delete
    fun deletePlace(placeName: Place)

    @Query("DELETE FROM places")
    fun deleteAllPlaces()
}