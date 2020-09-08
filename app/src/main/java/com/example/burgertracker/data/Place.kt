package com.example.burgertracker.data


import android.graphics.Bitmap
import android.util.Log
import com.example.burgertracker.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.gson.annotations.Expose


/**
 * @param results The list of results from the Places API Retrofit call indicating all the places retrieved
 * @param next_page_token The token indicting a next page of places (if available)
 * @param status The status code of the Places API call
 */
data class Result(

    @Expose
    var results: ArrayList<Place>,
    @Expose
    var next_page_token: String,
    @Expose
    var status: String
)

data class GeometryResult(

    @Expose
    var location: PlaceLocation
)

data class PlaceLocation(

    @Expose
    var lat: Double,
    @Expose
    var lng: Double
)

private const val TAG = "Place"

data class Place(
    @Expose
    var place_id: String,
    @Expose
    var name: String,
    @Expose
    var geometry: GeometryResult,
    @Expose
    var formatted_address: String,
    @Expose
    var rating: Double,
    @Expose
    var formatted_phone_number: String?,
    var imageString: String?
) {
    var photo_reference: Bitmap? = null
    var markerIcon: BitmapDescriptor? = null

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