package com.example.burgertracker.placesData


data class PlaceReview(
    var author_name: String,
    var rating: Int,
    var text: String,
    var relative_time_description: String
)
