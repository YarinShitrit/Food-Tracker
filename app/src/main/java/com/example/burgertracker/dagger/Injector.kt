package com.example.burgertracker.dagger

import com.example.burgertracker.ApplicationComponent
import javax.inject.Singleton

@Singleton
object Injector {

    lateinit var applicationComponent: ApplicationComponent

}