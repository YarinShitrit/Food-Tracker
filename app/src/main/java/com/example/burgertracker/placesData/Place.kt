package com.example.burgertracker.placesData


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.firebase.database.*
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "places")
data class Place(
    @PrimaryKey(autoGenerate = false)
    var place_id: String,
    var name: String,
    @Embedded
    var geometry: GeometryResult,
    var vicinity: String,
    var rating: Double,
    var formatted_phone_number: String? = "",
    var imageString: String? = ""
) : Serializable {
    var distance: Float? = null

    @get:Exclude
    @Ignore
    var totalFavorites: Long = 0L

    @get:Exclude
    @Ignore
    var markerIcon: BitmapDescriptor? = null

    @get:Exclude
    @Ignore
    var isLiked: Boolean = false

}

