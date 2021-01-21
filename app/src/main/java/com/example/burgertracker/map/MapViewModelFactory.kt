package com.example.burgertracker.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.burgertracker.AppRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapViewModelFactory @Inject constructor(private val appRepository: AppRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MapViewModel(appRepository) as T
    }
}