package com.example.burgertracker.models


import android.util.Log
import android.view.View
import com.example.burgertracker.databinding.InfoWindowBinding
import com.example.burgertracker.placesData.Place
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

private const val TAG = "PlaceInfoWindow"
class PlaceInfoWindow(
    private val binding: InfoWindowBinding,
    private val mapWrapperLayout: MapWrapperLayout,
    private val callButtonListener: OnInfoWindowElemTouchListener
) :
    GoogleMap.InfoWindowAdapter {
    private lateinit var mPlace: Place

    fun setPlace(place: Place) {
        Log.d(TAG,"setPlace() called with $place")
        mPlace = place
        binding.placeName.text = place.name
    }

    override fun getInfoContents(marker: Marker): View {
        Log.d("MapFragment", "InfoWindow getInfoContents ${this.hashCode()}")
        mapWrapperLayout.setMarkerWithInfoWindow(marker, binding.root)
        callButtonListener.setMarker(marker)
        return binding.root
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }
}