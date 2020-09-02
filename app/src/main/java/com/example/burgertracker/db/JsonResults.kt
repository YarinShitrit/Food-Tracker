package com.example.burgertracker.db

import com.google.gson.annotations.Expose

class JsonResults(

    @Expose
     var results: List<PlaceEntity>
)