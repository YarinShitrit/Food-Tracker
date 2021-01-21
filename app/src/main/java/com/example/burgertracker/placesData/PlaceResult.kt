package com.example.burgertracker.placesData

/**
 * @param results The list of results from the Places API Retrofit call indicating all the places retrieved
 * @param next_page_token The token indicting a next page of places (if available)
 * @param status The status code of the Places API call
 */
data class PlaceResult(

    var results: ArrayList<Place>,

    var next_page_token: String,

    var status: String
)