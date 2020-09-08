package com.example.burgertracker.map


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.burgertracker.AppUtils
import com.example.burgertracker.R
import com.example.burgertracker.data.AppReceiver
import com.example.burgertracker.models.MapWrapperLayout
import com.example.burgertracker.models.OnInfoWindowElemTouchListener
import com.example.burgertracker.settings.SettingsActivity
import com.example.burgertracker.toLatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.info_window_view.view.*
import kotlinx.android.synthetic.main.main_map_activity.*
import kotlinx.coroutines.runBlocking

private const val TAG = "MapActivity"
private const val NOTIFICATION_ID = 21
private const val PERMISSION_ID = 10

class MapActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var notificationHandler: NotificationCompat.Builder
    private lateinit var drawer: DrawerLayout
    private lateinit var infoWindow: PlaceInfoWindow
    private val normalZoom = 17.0F
    private var mapStatus = false //  true when onMapReady() is called
    private val permissionsNeeded = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET
    )
    private lateinit var appMap: GoogleMap
    private lateinit var mapViewModel: MapViewModel
    private lateinit var callButtonClickListener: OnInfoWindowElemTouchListener

    private fun createNotificationChannel() {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        Log.d(TAG, "createNotificationChannel called")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(21.toString(), name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun enableNotifications() {

        notificationHandler = NotificationCompat.Builder(this)
            .setChannelId("21")
            .setSmallIcon(R.drawable.hamburger)
            .setContentTitle("Burger Tracker ")
            .setContentText("New Food Ahead!")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(
                        this,
                        MapActivity::class.java
                    ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) },
                    0
                )
            )
            .addAction(
                R.drawable.hamburger,
                "Dismiss",
                getBroadcast(
                    this,
                    0,
                    Intent(this, AppReceiver::class.java).apply {
                        putExtra(
                            "notificationId",
                            NOTIFICATION_ID
                        )
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setAutoCancel(true)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {
        val checker = permissionsNeeded.all {
            checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
        Log.d(TAG, "checkPermissions called - returning $checker")
        return checker
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult called")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.all {
                it == PackageManager.PERMISSION_GRANTED
            }) {
            Log.d(TAG, " calling getNearbyPlaces from permissions result")
            mapViewModel.placesList.value?.clear()
            mapViewModel.getNearbyPlaces(null)
        } else {
            AppUtils().showPermissionsSnackBar(this)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.main_map_activity)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.main_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawer.addDrawerListener(toggle)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.mapItem -> {
                    Log.d(TAG, "map item clicked")
                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map_fragment) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
                R.id.settingsItem -> {
                    Log.d(TAG, " settings item clicked")
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
            }
            return@setNavigationItemSelectedListener true
        }
        toggle.syncState()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (savedInstanceState == null || navView.checkedItem?.itemId == R.id.mapItem) {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_fragment) as SupportMapFragment
            mapFragment.getMapAsync(this)

        }

    }

    override fun onStart() {
        Log.d(TAG, "onStart called")
        super.onStart()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        Log.d(TAG, "onResume called")
        super.onResume()
        /**checks if permissions were granted and onMapReady() was called already so the activity was just paused/stopped
         * so just display map again because onMapReady() is called only once
         */
        if (checkPermissions() && mapStatus) {
            AppUtils().getCurrentLocation(this)
            initMap()
            if (!mapViewModel.placesList.value.isNullOrEmpty()) {
                displayPlaces()
            }
        }
        /**checks if
         * no permissions were granted even after requestPermissions() was called from onMapReady() so the user
         * denied the permissionsRequest and now need to display snackbar
         */
        if (!checkPermissions() && mapStatus) {
            AppUtils().showPermissionsSnackBar(this)
        }
        map_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotEmpty() && checkPermissions()) {
                    Log.d(TAG, " calling getNearbyPlaces from query")
                    mapViewModel.placesList.value?.clear()
                    mapViewModel.getNearbyPlaces(query)
                    //clearFocus() closes the keyboard after performing the search
                    map_search.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady called")
        appMap = googleMap
        mapStatus = true
        initObservers()
        enableMarkersOnClick()
        if (!checkPermissions()) {
            requestPermissions(
                permissionsNeeded,
                PERMISSION_ID
            )

        } else {
            initMap()
            AppUtils().getCurrentLocation(this)
            if (!mapViewModel.placesList.value.isNullOrEmpty()) {
                displayPlaces()
            }
        }
    }

    private fun initObservers() {
        Log.d(TAG, "initObservers called")
        mapViewModel.userLocation.observe(this, Observer {
            Log.d(TAG, "userLocation changed -> $it")
            //initMap()
            appMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it, normalZoom
                )
            )
            appMap.setOnMyLocationButtonClickListener {
                appMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        it, normalZoom
                    )
                )
                true
            }

            if (mapViewModel.placesList.value.isNullOrEmpty()) {
                Log.d(TAG, "calling getNearbyPlaces from placesList observer")
                mapViewModel.getNearbyPlaces(null)
            }
        })
        mapViewModel.placesList.observe(this, Observer {
            if (!it.isNullOrEmpty()) {
                Log.d(TAG, "placesList changed -> calling displayPlaces()")
                displayPlaces()
            }
        })
    }

    private fun initMap() {
        Log.d(TAG, "initMap called")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        appMap.uiSettings.isMyLocationButtonEnabled = true
        appMap.uiSettings.isCompassEnabled = true
        appMap.isMyLocationEnabled = true
        appMap.uiSettings.isZoomControlsEnabled = true
        appMap.setOnMyLocationClickListener {
            appMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it.toLatLng(), appMap.cameraPosition.zoom
                )
            )
        }
        val infoWindowView = layoutInflater.inflate(R.layout.info_window_view, null) as ViewGroup
        callButtonClickListener =
            object : OnInfoWindowElemTouchListener(infoWindowView.findViewById(R.id.callButton)) {
                override fun onClickConfirmed(v: View?, marker: Marker?) {
                    Toast.makeText(
                        this@MapActivity,
                        "Number is ${infoWindow.mPlace.formatted_phone_number}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        val mapWrapperLayout = findViewById<View>(R.id.map_wrapper_layout) as MapWrapperLayout
        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        mapWrapperLayout.init(appMap, AppUtils().getPixelsFromDp(this, 39 + 20.toFloat()))
        infoWindow =
            PlaceInfoWindow(
                infoWindowView,
                mapWrapperLayout,
                callButtonClickListener
            )
        appMap.setInfoWindowAdapter(infoWindow)
        infoWindow.mWindow.callButton.setOnTouchListener(callButtonClickListener)

    }

    private fun displayPlaces() {
        Log.d(TAG, "displayPlaces called")
        appMap.clear()
        Log.d(TAG, "deleting duplicate places size is ${mapViewModel.placesList.value?.size}")
        mapViewModel.placesList.value?.distinct()
        Log.d(TAG, "deleted duplicate places size is ${mapViewModel.placesList.value?.size}")
        for (place in mapViewModel.placesList.value!!) {
            val markerOptions = MarkerOptions()
                .position(LatLng(place.geometry.location.lat, place.geometry.location.lng))
                .title(place.name)
                .icon(place.setIcon())
            appMap.addMarker(markerOptions)
        }

    }

    private fun enableMarkersOnClick() {
        Log.d(TAG, "enableMarkersOnClick called")
        appMap.setOnMarkerClickListener {
            val place = mapViewModel.placesList.value?.find { place ->
                LatLng(place.geometry.location.lat, place.geometry.location.lng) == it.position
            }!!
            infoWindow.setPlace(place)
            appMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        place.geometry.location.lat,
                        place.geometry.location.lng
                    ), appMap.cameraPosition.zoom
                )
            ).apply { Log.d(TAG, "Marker Location is ${appMap.cameraPosition.target}") }
            // TODO since the function returns false before the coroutine ends, it does not show place phone number and photo
            // TODO need to work on the coroutine to make the function return only after coroutine ends
            runBlocking {
                //   performSearchByPlace(place)
                //  marker.showInfoWindow()
            }
            false
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause called")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop called")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        super.onDestroy()
        mapStatus = false
        /* with(NotificationManagerCompat.from(this)) {
             // notificationId is a unique int for each notification that you must define
             notify(21, notificationHandler.build())
         }*/
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.mapItem -> {
                Log.d(TAG, " map clicked")
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map_fragment) as SupportMapFragment
                mapFragment.getMapAsync(this)
            }
            R.id.settingsItem -> {
                Log.d(TAG, " settings clicked")
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
        }
        return true
    }

    /**
     * checks if the navigation drawer is open and if so it will close it on back press otherwise close the activity
     */
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}




