package com.example.burgertracker.map

import com.example.burgertracker.R
import com.example.burgertracker.placesData.Place
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MarkerIconGenerator {

    fun setPlacesMarkerIcon(place: Place, queryIcon: String?) {
        if (!queryIcon.isNullOrEmpty()) {
            place.apply {
                when (queryIcon) {
                    "Pizza" -> markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.pizza)
                    "Burger" -> markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
                    "Sushi" -> markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.sushi)
                    "Mexican" -> markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.taco)
                    "Coffee" -> markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.cafe)
                }
            }
        }
        place.apply {
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
                    markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
                } else if (name.contains("sushi", true) || name.contains(
                        "סושי",
                        true
                    )
                ) {
                    markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.sushi)
                } else if (name.contains("cafe".trim(), true) || name.contains(
                        "קפה".trim(),
                        true
                    )
                ) {
                    markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.cafe)
                } else if (name.contains("taco".trim(), true)) {
                    markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.taco)
                }
            }
        }
    }
}