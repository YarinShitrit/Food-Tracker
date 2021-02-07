package com.example.burgertracker.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.burgertracker.placesData.Place
import kotlinx.coroutines.Deferred


@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    @Query("SELECT * FROM places")
    suspend fun getAllPlacesAsync(): List<Place>

    @Query("SELECT * FROM places WHERE place_id = :placeID")
    suspend fun getIfPlaceIsFavorite(placeID: String): Place

    @Delete
    suspend fun deletePlace(placeName: Place)

    @Query("DELETE FROM places")
    suspend fun deleteAllPlaces()
}