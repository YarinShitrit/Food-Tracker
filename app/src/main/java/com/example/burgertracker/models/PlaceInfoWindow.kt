package com.example.burgertracker.models


import android.view.View
import com.example.burgertracker.databinding.InfoWindowBinding
import com.example.burgertracker.placesData.Place
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class PlaceInfoWindow(
    val binding: InfoWindowBinding,
    private val mapWrapperLayout: MapWrapperLayout,
    private val callButtonListener: OnInfoWindowElemTouchListener
) :
    GoogleMap.InfoWindowAdapter {
    lateinit var mPlace: Place

    fun setPlace(place: Place) {
        mPlace = place
        binding.placeName.text = place.name
        binding.addressText.text = place.formatted_address.trim()
        binding.rating.text = "Rating: ${place.rating}"
        binding.distanceTextView.text = "${place.distance}km"
    }

    override fun getInfoContents(marker: Marker): View {
        mapWrapperLayout.setMarkerWithInfoWindow(marker, binding.root)
        callButtonListener.setMarker(marker)
        return binding.root

    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}