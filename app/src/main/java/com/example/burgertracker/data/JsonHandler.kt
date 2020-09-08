package com.example.burgertracker.data

import com.example.burgertracker.map.MapHandler
import com.example.burgertracker.map.MapActivity

private const val TAG = "JsonHandler"

class JsonHandler(private val mapHandler: MapHandler, private val activity: MapActivity) {
    /*
    private val appKey: String = mapHandler.activity.resources.getString(R.string.google_maps_key)
    private var createPlacesFlag = false
    private val mapViewModel: MapViewModel by activity.viewModels()

    fun getJsonStringResult(
        url: String,
        requestedPlace: Place?,
        pictureSearch: Boolean = false
    ) {
        Log.d(TAG, "GetJSONStringResult called")
        GlobalScope.launch {
            val jsonResponse: String
            /** we must call delay() because the page token takes time to fetch and may cause crashes*/
            delay(1350)
            Log.d(TAG, "URL is $url")
            val urlConnection: HttpURLConnection?
            val requestUrl = URL(url)
            urlConnection = requestUrl.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            urlConnection.doOutput = true
            urlConnection.connect()
            val br = BufferedReader(InputStreamReader(requestUrl.openStream()))
            jsonResponse = br.use(BufferedReader::readText)
            if (requestedPlace != null) {
                if (pictureSearch) {
                    Log.d(TAG, jsonResponse)
                    requestedPlace.photo_reference =
                        BitmapFactory.decodeStream(requestUrl.openConnection().getInputStream())
                    this.cancel()
                } else {
                    mapViewModel.placeJsonString += jsonResponse
                    Log.d(TAG, mapViewModel.placeJsonString!!)
                }
            } else {
                createPlacesFlag = true
                mapViewModel.jsonString += "\n$jsonResponse"
                if (jsonResponse.contains("next_page_token")) {
                    createPlacesFlag = false
                    val token = JSONObject(jsonResponse).getString("next_page_token")
                    val tokenUrl =
                        "https://maps.googleapis.com/maps/api/place/textsearch/json?key=$appKey&pagetoken=$token"
                    getJsonStringResult(tokenUrl, requestedPlace)
                }
            }
            br.close()

        }.invokeOnCompletion {
            if (requestedPlace == null && createPlacesFlag) {
                createPlacesFlag = false
                activity.runOnUiThread { createPlacesFromJSON() }
            } else if (requestedPlace != null && !pictureSearch) {
                addPlaceDetails(requestedPlace)
            }
        }
    }

    private fun createPlacesFromJSON() {
        Log.d(TAG, "createPlacesFromJSON called")
        mapViewModel.placesList.value!!.clear()

        val placesJSONArray = JSONObject(mapViewModel.jsonString).getJSONArray("results")
        for (num in 0 until placesJSONArray.length()) {
            val jsonPlace = placesJSONArray.getJSONObject(num)
            var location = LatLng(
                jsonPlace.getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat"),
                jsonPlace.getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng")
            )
            val placeLocation = (location.latitude, location.longitude)
            try {
                val photoReference = jsonPlace.getJSONArray("photos").getJSONObject(0)
                    .getString("photo_reference")
                val place = Place(
                    jsonPlace.getString("place_id"),
                    jsonPlace.getString("name"),
                    placeLocation,
                    jsonPlace.getString("formatted_address"),
                    jsonPlace.getDouble("rating"),
                    photoReference,
                    null
                )
                mapViewModel.placesList.value!!.add(place)
            } catch (e: JSONException) {
                Log.e(TAG, " $e \n Failed to get JSON photo data from $jsonPlace")
                val place = Place(
                    jsonPlace.getString("place_id"),
                    jsonPlace.getString("name"),
                    placeLocation,
                    jsonPlace.getString("formatted_address"),
                    jsonPlace.getDouble("rating"),
                    null,
                    null
                )
                mapViewModel.placesList.value!!.add(place)
            }
        }
        //mapHandler.displayPlaces()
    }

    private fun addPlaceDetails(requestedPlace: Place) {
        Log.d(TAG, "addPlaceDetails called")
        // Log.d(TAG, "places string is ${mapViewModel.placeJsonString}")
        if (mapViewModel.placeJsonString!!.contains("formatted_phone_number")) {
            Log.d(
                TAG,
                "phone number is ${
                    JSONObject(mapViewModel.placeJsonString!!).getJSONObject("result")
                        .getString("formatted_phone_number")
                }"
            )
            requestedPlace.formatted_phone_number =
                JSONObject(mapViewModel.placeJsonString!!).getJSONObject("result")
                    .getString("formatted_phone_number")
        } else {
            requestedPlace.formatted_phone_number = "No Number Available"
        }
    }
*/
}