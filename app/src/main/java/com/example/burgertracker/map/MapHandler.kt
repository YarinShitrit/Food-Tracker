package com.example.burgertracker.map


import com.google.android.gms.maps.GoogleMap

private const val TAG = "MapHandler"
private const val BASE_URL = "https://maps.googleapis.com"

class MapHandler(private var appMap: GoogleMap, val activity: MapActivity) {
    /*
    private val mapViewModel: MapViewModel by activity.viewModels()
    private val app_key: String = activity.resources.getString(R.string.google_maps_key)
    private lateinit var infoWindow: PlaceInfoWindow
    private lateinit var userLocation: LatLng
    private val normalZoom = 17.0F
    private val jsonHandler = JsonHandler(this, activity)

    init {
        Log.d(TAG, "MapHandler created")
        //getCurrentLocation()
        //displayMap()
    }

    /*
        private fun getCurrentLocation() {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                &&
                (ActivityCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                val currentLocationTask =
                    LocationServices.getFusedLocationProviderClient(activity).lastLocation
                currentLocationTask.addOnCompleteListener(activity) {
                    Log.d(TAG, "CurrentLocationTask Processing")
                    if (currentLocationTask.isSuccessful) {
                        Log.d(TAG, "CurrentLocationTask Completed")
                        val currentLocation = currentLocationTask.result
                        appMap.isMyLocationEnabled = true
                        appMap.uiSettings.isZoomControlsEnabled = true
                        if (currentLocation != null) {
                            try {
                                userLocation =
                                    LatLng(currentLocation.latitude, currentLocation.longitude)
                                appMap.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        userLocation, normalZoom
                                    )
                                )
                                appMap.setOnMyLocationButtonClickListener {
                                    appMap.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            userLocation, normalZoom
                                        )
                                    )
                                    Log.d(TAG, "Current Location is ${appMap.cameraPosition.target}")
                                    true
                                }
                                performSearch(null)
                            } catch (e: NullPointerException) {
                                Log.d(TAG, "Could Not Display Map $currentLocation is the location")
                            }
                        }
                    }
                }
            } else {

            }
        }
    */
    private fun performSearchByPlace(requestedPlace: Place) {
        Log.d(TAG, "performSearchByPlace called")
        /** checks if the place is already in the viewModel data
         * so just send the place instead of performing an api request*/
        if (requestedPlace.formatted_phone_number == null || requestedPlace.photo_reference == null) {
            mapViewModel.placeJsonString = ""
            val searchURL =
                ("https://maps.googleapis.com/maps/api/place/details/json?placeid=${requestedPlace.place_id}&key=$app_key")
            val pictureSearchURL =
                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=${requestedPlace.imageString}&key=$app_key"
            Log.d(TAG, "Place request - $searchURL")
            Log.d(TAG, "Photo request - $pictureSearchURL")
            jsonHandler.getJsonStringResult(pictureSearchURL, requestedPlace, true)
            jsonHandler.getJsonStringResult(searchURL, requestedPlace)
        }
    }

    fun performSearch(foodType: String?, querySearch: Boolean = false) {
/*
        mapViewModel.jsonString = ""
        val radius = 1000 *
                activity.getSharedPreferences("prefs", Context.MODE_PRIVATE).getInt("range", 25)
        Log.d(TAG, "radius is $radius")
        Log.d(TAG, "performSearch called")
        /**
         * foodType will be null at application launch to show all places nearby
         * checks if orientation event cause the onCreate in order to display places from viewModel
         * instead of performing api request
         */
        if (!mapViewModel.placesList.value.isNullOrEmpty() && !querySearch) {
            displayPlaces()
        } else {
            val searchURL: String = if (foodType != null) {
                ("https://maps.googleapis.com/maps/api/place/textsearch/json?query=${foodType}&&types=restaurant&location=${userLocation.latitude},${userLocation.longitude}&radius=$radius&key=$app_key")
            } else {
                "https://maps.googleapis.com/maps/api/place/textsearch/json?types=restaurant&location=${userLocation.latitude},${userLocation.longitude}&radius=$radius&key=$app_key"
            }
            jsonHandler.getJsonStringResult(searchURL, null)
        }*/
        // performSearchByRetrofit()

    }

    private fun performSearchByRetrofit(foodType: String?, querySearch: Boolean = false) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PlacesRetrofitInterface::class.java)
        Log.d(TAG, "LOCATION: ${userLocation.latitude}, ${userLocation.longitude}")
        service.getNearbyPlaces(
            "Nam",
            "${userLocation.latitude}, ${userLocation.longitude}",
            "restaurants",
            1000,
            app_key
        )
            .enqueue(object : Callback<Result> {
                override fun onFailure(p0: Call<Result>, p1: Throwable) {
                    Log.e(TAG, "failed" + p1.localizedMessage)
                }

                override fun onResponse(
                    p0: Call<Result>,
                    p1: Response<Result>
                ) {
                    if (p1.isSuccessful) {
                        Log.d(TAG, "success" + p1.body()?.results.toString())
                        Log.d(TAG, p0.toString())
                    } else {
                        Log.e(TAG, "was not successful $p1.message()")
                    }
                }
            })
    }
/*
    fun displayPlaces() {
        Log.d(TAG, "displayPlaces called")
        appMap.clear()
        // mapViewModel.markersList.value!!.clear()
        for (num in 0 until mapViewModel.placesList.value!!.size) {
            //   Log.d(TAG, "displaying ${placesArray[num]}")
            Log.d(TAG, mapViewModel.placesList.value!![num].markerIcon?.javaClass.toString())
            val marker = appMap.addMarker(
                MarkerOptions()
                    .title(mapViewModel.placesList.value!![num].name)
                    .position(mapViewModel.placesList.value!![num].geometry)
                    .icon(mapViewModel.placesList.value!![num].markerIcon)
                    .snippet("Rating: ${mapViewModel.placesList.value!![num].rating}")
            )
            //mapViewModel.markersList.value!!.add(marker)
        }
    }*/
*/
}


