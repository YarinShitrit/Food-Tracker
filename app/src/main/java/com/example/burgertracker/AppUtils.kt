package com.example.burgertracker

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.burgertracker.data.Place
import com.example.burgertracker.map.MapActivity
import com.example.burgertracker.map.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.main_map_activity.*
import java.lang.Exception


private const val TAG = "AppUtils"

class AppUtils : Application() {

    /**
     * Shows a SnackBar on [MapActivity] that redirects to app settings in order to enable location permission
     * @param activity The [MapActivity] associated with the application
     */
    fun showPermissionsSnackBar(activity: MapActivity) {
        Log.d(TAG, "showPermissionsSnackBar called")
        Snackbar.make(
            activity.map,
            "Please Enable Location Permission",
            Snackbar.LENGTH_INDEFINITE
        ).setAction(
            "Enable Location"
        ) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            activity.startActivityForResult(settingsIntent, 1)

        }
            .show()
    }

    /**
     * Gets the user current location and updates [MapViewModel.userLocation] accordingly
     * @param activity The activity that calls the method, Necessary for [LocationServices.getFusedLocationProviderClient]
     */
    fun getCurrentLocation(activity: AppCompatActivity) {
        Log.d(TAG, "getCurrentLocation called")
        val viewModel = ViewModelProvider(activity).get(MapViewModel::class.java)
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            &&
            (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            try {
                val currentLocationTask =
                    LocationServices.getFusedLocationProviderClient(activity).lastLocation
                currentLocationTask.addOnSuccessListener(activity) {
                    Log.d(TAG, "CurrentLocationTask Processing")
                    if (currentLocationTask.isSuccessful) {
                        Log.d(
                            TAG,
                            "CurrentLocationTask Completed Successfully, Current location is $it"
                        )
                        viewModel.userLocation.value = it.toLatLng()
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Could not receive current location -> ${e.localizedMessage}")
            }
        }
    }

    fun setPlaceMarkerIcon(placesList: ArrayList<Place>, query: String?) {
        placesList.forEach {
            var place = it
            if (place.markerIcon == null && !query.isNullOrEmpty()) {

                when (query) {
                    "pizza" -> place.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.pizza)
                    "burger" -> place.markerIcon =
                        BitmapDescriptorFactory.fromResource(R.drawable.hamburger)

                }
                if (place.name.contains("pizza".trim(), true) || place.name.contains(
                        "pizzeria".trim(),
                        true
                    ) || place.name.contains("פיצה".trim(), true)
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.pizza)
                } else if (place.name.contains("burger".trim(), true) || place.name.contains(
                        "בורגר".trim(),
                        true
                    )
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.hamburger)
                } else if (place.name.contains("sushi", true) || place.name.contains(
                        "סושי",
                        true
                    )
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.sushi)
                } else if (place.name.contains("cafe".trim(), true) || place.name.contains(
                        "קפה".trim(),
                        true
                    )
                ) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.cafe)
                } else if (place.name.contains("taco".trim(), true)) {
                    place.markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.taco)
                }
            }
        }
    }

    fun getPixelsFromDp(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

}

/**
 * @return a [LatLng] object from the [Location] object
 */
fun Location.toLatLng() = LatLng(this.latitude, this.longitude)