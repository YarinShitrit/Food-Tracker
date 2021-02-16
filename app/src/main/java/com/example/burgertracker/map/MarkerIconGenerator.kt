package com.example.burgertracker.map

import com.example.burgertracker.R
import com.example.burgertracker.placesData.Place
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MarkerIconGenerator {

    fun setPlacesMarkerIcon(list: ArrayList<Place>, queryIcon: String?) {
        if (!queryIcon.isNullOrEmpty()) {
            list.forEach {
                when (queryIcon) {
                    "Pizza" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.pizza)
                    "Burger" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
                    "Sushi" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.sushi)
                    "Mexican" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.taco)
                    "Coffee" -> it.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.cafe)
                }
            }
        }
        list.forEach {
            val place = it
            if (place.markerIcon == null) {
                if (place.name.contains("pizza".trim(), true) || place.name.contains(
                        "pizzeria".trim(),
                        true
                    ) || place.name.contains("פיצה".trim(), true)
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.pizza)
                } else if (place.name.contains("burger".trim(), true) || place.name.contains(
                        "בורגר".trim(),
                        true
                    )
                ) {
                    place.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
                } else if (place.name.contains("sushi", true) || place.name.contains(
                        "סושי",
                        true
                    )
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.sushi)
                } else if (place.name.contains("cafe".trim(), true) || place.name.contains(
                        "קפה".trim(),
                        true
                    )
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.cafe)
                } else if (place.name.contains("taco".trim(), true)) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.taco)
                }
            }
        }
    }
}