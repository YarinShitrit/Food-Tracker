package com.example.burgertracker.map

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.burgertracker.models.MapWrapperLayout
import com.example.burgertracker.models.OnInfoWindowElemTouchListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.example.burgertracker.R
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment


private const val TAG = "MapTestActivity"

class MapTestActivity : AppCompatActivity(), OnMapReadyCallback {
    private var infoWindow: ViewGroup? = null
    private var placeName: TextView? = null
    private var placeLocation: TextView? = null
    private var callButton: ImageButton? = null
    private var infoButtonListener: OnInfoWindowElemTouchListener? = null
    lateinit var map: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_map_layout)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.testMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    companion object {
        fun getPixelsFromDp(context: Context, dp: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "onMapReady called")
        map = p0
        val mapWrapperLayout = findViewById<View>(R.id.map_relative_layout) as MapWrapperLayout
        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        mapWrapperLayout.init(map, getPixelsFromDp(this, 39 + 20.toFloat()))

        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        infoWindow = layoutInflater.inflate(R.layout.info_window_view, null) as ViewGroup?
        placeName = infoWindow!!.findViewById<View>(R.id.placeName) as TextView
        placeLocation = infoWindow!!.findViewById<View>(R.id.placeLocation) as TextView
        callButton = infoWindow!!.findViewById<View>(R.id.callButton) as ImageButton

        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up
        infoButtonListener = object : OnInfoWindowElemTouchListener(
            callButton!!

        ) {
            override fun onClickConfirmed(v: View?, marker: Marker?) {
                // Here we can perform some action triggered after clicking the button
                Toast.makeText(
                    this@MapTestActivity,
                    marker!!.title + "'s button clicked!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        callButton!!.setOnTouchListener(infoButtonListener)
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Setting up the infoWindow with current's marker info
                placeName!!.text = marker.title
                placeLocation!!.text = marker.snippet
                (infoButtonListener as OnInfoWindowElemTouchListener).setMarker(marker)
                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow)
                return infoWindow!!
            }
        })

        // Let's add a couple of markers
        map.addMarker(
            MarkerOptions()
                .title("Prague")
                .snippet("Czech Republic")
                .position(LatLng(50.08, 14.43))
        )
        map.addMarker(
            MarkerOptions()
                .title("Paris")
                .snippet("France")
                .position(LatLng(48.86, 2.33))
        )
        map.addMarker(
            MarkerOptions()
                .title("London")
                .snippet("United Kingdom")
                .position(LatLng(51.51, -0.1))
        )
    }
}