package com.example.burgertracker

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi


private const val TAG = "Util"
private val PermissionsNeeded = arrayOf(
    android.Manifest.permission.ACCESS_COARSE_LOCATION,
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    android.Manifest.permission.INTERNET
)

class Util : Application() {
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions(): Boolean {

        var checker = true
        for (Permission in PermissionsNeeded) {
            if (checkSelfPermission(Permission) == PackageManager.PERMISSION_DENIED) {
                checker = false
                break
            }
        }
        Log.d(TAG, "checkPermissions called - returning $checker")
        return checker
    }



}