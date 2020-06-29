package com.example.burgertracker

import android.content.Context
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


class MapWrapperLayout(context: Context, googleMap: GoogleMap, pixels: Int) :
    RelativeLayout(context) {

    private val map: GoogleMap = googleMap
    private val bottomOffsetPixels = pixels
    private var mInfoWindow: View? = null
    private var mMarker: Marker? = null
    fun setMarkerWithInfoWindow(marker: Marker, infoWindow: View) {
        mInfoWindow = infoWindow
        mMarker = marker
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        var ret = false
        // Make sure that the infoWindow is shown and we have all the needed references
        // Make sure that the infoWindow is shown and we have all the needed references
        if (mMarker != null && mMarker!!.isInfoWindowShown && mInfoWindow != null) {
            // Get a marker position on the screen
            val point: Point = map.projection.toScreenLocation(mMarker!!.position)

            // Make a copy of the MotionEvent and adjust it's location
            // so it is relative to the infoWindow left top corner
            val copyEv = MotionEvent.obtain(ev)
            copyEv.offsetLocation(
                (-point.x + mInfoWindow!!.width / 2).toFloat(),
                (-point.y + mInfoWindow!!.height + bottomOffsetPixels).toFloat()
            )

            // Dispatch the adjusted MotionEvent to the infoWindow
            ret = mInfoWindow!!.dispatchTouchEvent(copyEv)
        }
        // If the infoWindow consumed the touch event, then just return true.
        // Otherwise pass this event to the super class and return it's result
        // If the infoWindow consumed the touch event, then just return true.
        // Otherwise pass this event to the super class and return it's result
        return ret || super.dispatchTouchEvent(ev)
    }
}