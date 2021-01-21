package com.example.burgertracker.placesData

import androidx.room.Embedded
import androidx.room.PrimaryKey

data class GeometryResult(
    @PrimaryKey(autoGenerate = true)
    var geoID : Long,
    @Embedded
    var location: PlaceLocation
)