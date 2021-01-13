package com.example.burgertracker

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.burgertracker.map.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng


private const val TAG = "AppUtils"

class AppUtils : Application() {

    /**
     * Gets the user current location and updates [MapViewModel.userLocation] accordingly
     * @param activity The activity that calls the method, Necessary for [LocationServices.getFusedLocationProviderClient]
     */
    fun getCurrentLocation(activity: FragmentActivity) {
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
                //Try to get location from LocationManager
                val nManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
                if (nManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val locationGPS: Location? =
                        nManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (locationGPS != null) {
                        Log.d(
                            TAG,
                            "Received location from LocationManager -> Current location is ${locationGPS.toLatLng()}"
                        )
                        viewModel.userLocation.value = locationGPS.toLatLng()
                    } else {
                        //Try to get location from LocationServices
                        val currentLocationTask =
                            LocationServices.getFusedLocationProviderClient(activity).lastLocation
                        currentLocationTask.addOnSuccessListener(activity) {
                            if (currentLocationTask.isSuccessful) {
                                Log.d(
                                    TAG,
                                    "Received Location from LocationServices -> Current location is ${it.toLatLng()}"
                                )
                                if (it != null) {
                                    viewModel.userLocation.value = it.toLatLng()
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "GPS not available")
                    Toast.makeText(activity, "Turn on GPS", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d(TAG, "Could not receive current location -> ${e.localizedMessage}")
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

