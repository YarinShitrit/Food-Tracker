package com.example.burgertracker.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class PlaceEntity(
    // @PrimaryKey(autoGenerate = true) private var placeID: Int,
    @Expose
    private var name: String,
    // @ColumnInfo(name = "phone_number") private var phoneNumber: String?,
    @Expose
    private var rating: Double

)