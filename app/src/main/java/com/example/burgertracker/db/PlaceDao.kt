package com.example.burgertracker.db


import androidx.room.*
import com.example.burgertracker.placesData.Place


@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place)

    @Query("SELECT * FROM places")
    suspend fun getAllPlacesAsync(): List<Place>

    @Query("SELECT * FROM places ORDER BY distance ASC")
    suspend fun getAllPlacesByDistance(): List<Place>

    @Query("SELECT * FROM places WHERE place_id = :placeID")
    suspend fun getIfPlaceIsFavorite(placeID: String): Place?

    @Query("SELECT * FROM places WHERE place_id = :placeID")
    suspend fun getPlaceFavorites(placeID: String): Place?
/*
    @Query("UPDATE places SET totalFavorites = :totalFavorites WHERE place_id =:placeID")
    suspend fun updateTotalFavorites(placeID: String, totalFavorites: Long)*/

    @Query("DELETE FROM places WHERE place_id =:placeID")
    suspend fun deletePlace(placeID: String)

    @Query("DELETE FROM places")
    suspend fun deleteAllPlaces()
}