package com.example.burgertracker.user


data class User(
    val id: String,
    var username: String? = "",
    var email: String? = "",
    var fbToken: String? = ""
)

