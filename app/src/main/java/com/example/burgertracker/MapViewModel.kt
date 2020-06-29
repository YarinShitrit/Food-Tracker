package com.example.burgertracker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.Marker

class MapViewModel : ViewModel() {
    val markersList = MutableLiveData<ArrayList<Marker>>()
    val placesList = MutableLiveData<ArrayList<Place>>()
    lateinit var jsonString: String
    var placeJsonString: String? = null

    init {
        markersList.value = arrayListOf()
        placesList.value = arrayListOf()
    }
}