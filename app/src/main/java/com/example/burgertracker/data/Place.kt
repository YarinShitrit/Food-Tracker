package com.example.burgertracker.data


import android.graphics.Bitmap
import android.util.Log
import com.example.burgertracker.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory


/**
 * @param results The list of results from the Places API Retrofit call indicating all the places retrieved
 * @param next_page_token The token indicting a next page of places (if available)
 * @param status The status code of the Places API call
 */
data class PlaceResult(

    var results: ArrayList<Place>,

    var next_page_token: String,

    var status: String
)

data class GeometryResult(

    var location: PlaceLocation
)

data class PlaceLocation(

    var lat: Double,

    var lng: Double
)

data class OpeningHours(
    var open_now: Boolean
)

private const val TAG = "Place"

data class Place(

    var place_id: String,

    var name: String,

    var geometry: GeometryResult,

    var formatted_address: String,

    var rating: Double,

    var opening_hours: OpeningHours,

    var formatted_phone_number: String?,
    var imageString: String?
) {
    var distance: Float? = null
    var markerIcon: BitmapDescriptor? = null
    var isLiked: Boolean = false
    fun setIcon(): BitmapDescriptor? {
        if (markerIcon == null) {
            if (name.contains("pizza".trim(), true) || name.contains(
                    "pizzeria".trim(),
                    true
                ) || name.contains("פיצה".trim(), true)
            ) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.pizza)
            } else if (name.contains("burger".trim(), true) || name.contains(
                    "בורגר".trim(),
                    true
                )
            ) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
            } else if (name.contains("sushi", true) || name.contains("סושי", true)) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.sushi)
            } else if (name.contains("cafe".trim(), true) || name.contains("קפה".trim(), true)) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.cafe)
            } else if (name.contains("taco".trim(), true)) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.taco)
            }
        }
        Log.d(TAG, " returning marker icon - $markerIcon")
        return markerIcon
    }
}