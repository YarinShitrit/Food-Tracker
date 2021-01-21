package com.example.burgertracker.placesData


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.BitmapDescriptor


@Entity(tableName = "places")
data class Place(
    @PrimaryKey(autoGenerate = false)
    var place_id: String,
    var name: String,
    @Embedded
    var geometry: GeometryResult,
    var formatted_address: String,
    var rating: Double,
    @Embedded
    var opening_hours: OpeningHours,
    var formatted_phone_number: String? = "",
    var imageString: String? = ""
) {
    var distance: Float? = null

    @Ignore
    var markerIcon: BitmapDescriptor? = null
}