package com.example.burgertracker.placesData

import androidx.room.PrimaryKey

data class PlaceLocation(
    @PrimaryKey(autoGenerate = true)
    var placeLocationId: Long,
    var lat: Double,
    var lng: Double
)