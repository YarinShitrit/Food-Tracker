package com.example.burgertracker.placesData

import androidx.room.Embedded

data class GeometryResult(
    @Embedded
    var location: PlaceLocation
)