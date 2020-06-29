package com.example.burgertracker


import android.graphics.Bitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng


class Place(
    var id: String,
    var name: String,
    var location: LatLng,
    var address: String,
    var rating: Double,
    var imageString: String?,
    var phoneNumber: String?
) {
    var iconImage: Bitmap? = null
    var markerIcon: BitmapDescriptor? = null


    init {
        if (markerIcon == null) {
            if (name.contains("pizza", true) || name.contains(
                    "pizzeria",
                    true
                ) || name.contains("פיצה", true)
            ) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.pizza)
            } else if (name.contains("burger", true) || name.contains("בורגר", true)) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
            } else if (name.contains("sushi", true) || name.contains("סושי", true)) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.sushi)
            } else if (name.contains("cafe", true) || name.contains("קפה", true)) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.cafe)
            } else if (name.contains("taco", true)) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.taco)
            }
            if (markerIcon == null) {
                markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.resturant)
            }
        }
    }

    override fun toString(): String {
        return "\n name:$name , location:${location.latitude}, ${location.longitude} \n address:$address , rating:$rating \n"
    }
}