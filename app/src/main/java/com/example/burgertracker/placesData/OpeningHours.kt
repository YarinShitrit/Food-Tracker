package com.example.burgertracker.placesData

import androidx.room.PrimaryKey

data class OpeningHours(
    @PrimaryKey(autoGenerate = true)
    var placeOwnerId : Long,
    var open_now: Boolean
)