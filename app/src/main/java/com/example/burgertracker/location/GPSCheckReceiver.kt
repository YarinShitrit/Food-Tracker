package com.example.burgertracker.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.util.Log

private const val TAG = "GPSCheckerReceiver"

class GPSCheckReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val locationManager =
            context.getSystemService(LOCATION_SERVICE) as LocationManager
        if (intent.action == Intent.ACTION_PROVIDER_CHANGED) {
            Log.d(TAG, "GPS Status Changed")
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) Log.d(
                TAG,
                "GPS ON"
            ) else Log.d(TAG, "GPS OFF")
        }
    }
}