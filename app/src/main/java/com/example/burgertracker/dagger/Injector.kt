package com.example.burgertracker.dagger

import com.example.burgertracker.ApplicationComponent
import com.example.burgertracker.DaggerApplicationComponent
import javax.inject.Singleton

@Singleton
object Injector {

    lateinit var applicationComponent: ApplicationComponent
    fun buildDaggerAppComponent() {
        applicationComponent = DaggerApplicationComponent.builder().build()
    }
}