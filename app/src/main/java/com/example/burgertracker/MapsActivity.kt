package com.example.burgertracker


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.getBroadcast
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.Window
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.internal.NavigationMenu
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_map_activity.*

lateinit var drawer: DrawerLayout
private const val TAG = "MapActivity"
private const val NOTIFICATION_ID = 21
private const val PERMISSION_ID = 10
private var MAP_STATUS = false //  true when onMapReady() is called
private var permissionsGranted = false // true when permissions granted
private var permissionsRequestCallback = false
private val PermissionsNeeded = arrayOf(
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.INTERNET
)
private var mapHandler: MapHandler? = null
private lateinit var appMap: GoogleMap


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var notificationHandler: NotificationCompat.Builder

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
                        MapsActivity::class.java
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
                    }
                    ,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .setAutoCancel(true)
    }

    private fun permissionSnackBar() {
        Log.d(TAG, "permissionSnackBar called")
        Snackbar.make(
            map,
            "Please Enable Location Permission",
            Snackbar.LENGTH_INDEFINITE
        ).setAction(
            "Enable Location"
        ) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(settingsIntent, 1)

        }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {

        var checker = true
        for (Permission in PermissionsNeeded) {
            if (this.checkSelfPermission(Permission) == PackageManager.PERMISSION_DENIED) {
                checker = false
                break
            }
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
        if (checkPermissions()) {
            permissionsGranted = true
            mapHandler = MapHandler(appMap, this)

        }
        permissionsRequestCallback = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
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
                    Log.d(TAG, " map clicked")
                    val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map_fragment) as SupportMapFragment
                    mapFragment.getMapAsync(this)
                }
                R.id.settingsItem -> {
                    Log.d(TAG, " settings clicked")
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
        map_search.queryHint = "Enter Food Type"
        enableNotifications()
        createNotificationChannel()

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
        if (checkPermissions() && MAP_STATUS) {
            if (mapHandler == null) {
                mapHandler = MapHandler(appMap, this)
            } else {
                mapHandler!!.displayMap()
            }
        }
        /**checks if
         * no permissions were granted even after requestPermissions() was called from onMapReady() so the user
         * denied the permissionsRequest and now need to display snackbar
         */
        if (!checkPermissions() && MAP_STATUS) {
            permissionSnackBar()
        }
        map_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotEmpty()) {
                    mapHandler?.performSearch(query, true)
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
        Log.d(TAG, "omMapReady called")
        appMap = googleMap
        MAP_STATUS = true
        if (!checkPermissions()) {
            requestPermissions(
                PermissionsNeeded,
                PERMISSION_ID
            )

        } else {
            mapHandler = MapHandler(appMap, this)
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
        MAP_STATUS = false
        /* with(NotificationManagerCompat.from(this)) {
             // notificationId is a unique int for each notification that you must define
             notify(21, notificationHandler.build())
         }*/
    }


}




