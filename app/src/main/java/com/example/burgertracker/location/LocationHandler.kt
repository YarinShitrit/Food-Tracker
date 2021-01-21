package com.example.burgertracker.location

import android.app.Activity
import android.location.Location

interface LocationHandler {

    fun getCurrentLocationFromLocationManager(activity: Activity) : Location?

    fun getCurrentLocationFromLocationServices() : Location?
}