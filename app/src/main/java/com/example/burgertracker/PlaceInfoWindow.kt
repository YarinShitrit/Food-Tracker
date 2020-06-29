package com.example.burgertracker


import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.info_window_view.view.*

class PlaceInfoWindow(activity: MapsActivity, private val place: Place) :
    GoogleMap.InfoWindowAdapter {
    private var mWindow = activity.layoutInflater.inflate(R.layout.info_window_view, null)

    private fun setWindowDetails() {

        mWindow.placeName.text = place.name
        mWindow.placeLocation.text = place.address
        mWindow.place_rating.text = "Rating:" + place.rating.toString()
        mWindow.phoneNumber.text = place.phoneNumber
        mWindow.imageView.setImageBitmap(place.iconImage)
    }

    override fun getInfoContents(p0: Marker?): View? {
        setWindowDetails()
        return mWindow

    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}