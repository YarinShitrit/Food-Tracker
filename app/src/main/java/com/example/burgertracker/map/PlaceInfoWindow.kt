package com.example.burgertracker.map


import android.view.View
import android.view.ViewGroup
import com.example.burgertracker.R
import com.example.burgertracker.data.Place
import com.example.burgertracker.models.MapWrapperLayout
import com.example.burgertracker.models.OnInfoWindowElemTouchListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.info_window_view.view.*

class PlaceInfoWindow(
    val mWindow: ViewGroup,
    private val mapWrapperLayout: MapWrapperLayout,
    private val callButtonListener: OnInfoWindowElemTouchListener
) :
    GoogleMap.InfoWindowAdapter {
    lateinit var mPlace: Place
    fun setPlace(place: Place) {
        mPlace = place
        mWindow.placeName.text = place.name
        mWindow.placeLocation.text = place.formatted_address
        mWindow.place_rating.text = "Rating: ${place.rating}"
        mWindow.phoneNumber.text = place.formatted_phone_number
        mWindow.imageView.setImageBitmap(place.photo_reference)
    }

    override fun getInfoContents(marker: Marker?): View? {
        mapWrapperLayout.setMarkerWithInfoWindow(marker, mWindow)
        callButtonListener.setMarker(marker)
        return mWindow

    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}