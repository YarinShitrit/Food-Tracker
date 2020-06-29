package com.example.burgertracker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) private var placeID: Int,
    private var name: String,
    @ColumnInfo(name = "phone_number") private var phoneNumber: String?,
    private var rating: Double

)