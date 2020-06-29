package com.example.burgertracker

import android.R
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_map_activity.*


open class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private var infoWindow: ViewGroup? = null
    private var infoTitle: TextView? = null
    private var infoSnippet: TextView? = null
    private var infoButton: Button? = null
    lateinit var map: GoogleMap
    private var infoButtonListener: GoogleMap.OnInfoWindowClickListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.burgertracker.R.layout.activity_main)
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(com.example.burgertracker.R.id.mainmap) as SupportMapFragment


        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        val mapWrapperLayout : MapWrapperLayout =
            MapWrapperLayout(this,map, getPixelsFromDp(this, 39 + 20.toFloat()))

        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        infoWindow = layoutInflater.inflate(com.example.burgertracker.R.layout.info_window_view, null) as ViewGroup?
        infoTitle = infoWindow!!.findViewById<View>(R.id.title) as TextView
        infoSnippet = infoWindow!!.findViewById<View>(R.id.snippet) as TextView
        infoButton = infoWindow!!.findViewById<View>(R.id.button) as Button

        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up
        infoButtonListener = object : GoogleMap.OnInfoWindowClickListener {
            protected fun onClickConfirmed(v: View?, marker: Marker) {
                // Here we can perform some action triggered after clicking the button
                Toast.makeText(
                    this@MainActivity,
                    marker.getTitle().toString() + "'s button clicked!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onInfoWindowClick(p0: Marker?) {
                TODO("Not yet implemented")
            }
        }
        infoButton!!.setOnTouchListener(infoButtonListener)
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker?): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View? {
                // Setting up the infoWindow with current's marker info
                infoTitle!!.setText(marker.getTitle())
                infoSnippet!!.setText(marker.getSnippet())
                infoButtonListener.setMarker(marker)

                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow!!)
                return infoWindow
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

    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            map =p0
        }
    }
    companion object {
        fun getPixelsFromDp(context: Context, dp: Float): Int {
            val scale: Float = context.getResources().getDisplayMetrics().density
            return (dp * scale + 0.5f).toInt()
        }
    }
}