package com.example.burgertracker.map


import android.view.View
import android.view.ViewGroup
import com.example.burgertracker.data.Place
import com.example.burgertracker.models.MapWrapperLayout
import com.example.burgertracker.models.OnInfoWindowElemTouchListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.info_window.view.*

class PlaceInfoWindow(
    val mWindow: ViewGroup,
    private val mapWrapperLayout: MapWrapperLayout,
    private val callButtonListener: OnInfoWindowElemTouchListener
) :
    GoogleMap.InfoWindowAdapter {
    lateinit var mPlace: Place
    fun setPlace(place: Place) {
        mPlace = place
        mWindow.place_name.text = place.name
        mWindow.address_text.text = place.formatted_address.trim()
        mWindow.rating.text = "Rating: ${place.rating}"
        mWindow.distance_text_view.text = "${place.distance}km"
    }

    override fun getInfoContents(marker: Marker): View? {
        mapWrapperLayout.setMarkerWithInfoWindow(marker, mWindow)
        callButtonListener.setMarker(marker)
        return mWindow

    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}