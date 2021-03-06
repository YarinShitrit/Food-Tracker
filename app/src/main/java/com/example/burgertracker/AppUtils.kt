package com.example.burgertracker

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.dagger.NetworkModule
import com.example.burgertracker.dagger.PlacesModule
import com.example.burgertracker.favorites.FavoritesFragment
import com.example.burgertracker.login.LoginFragment
import com.example.burgertracker.map.DetailedPlaceFragment
import com.example.burgertracker.map.MapActivity
import com.example.burgertracker.map.MapFragment
import com.example.burgertracker.settings.SettingsFragment
import com.google.android.gms.maps.model.LatLng
import dagger.Component
import java.util.prefs.Preferences
import javax.inject.Singleton

@Singleton
@Component(modules = ([NetworkModule::class, PlacesModule::class]))
interface ApplicationComponent {

    fun inject(activity: MapActivity)
    fun inject(fragment: MapFragment)
    fun inject(fragment: LoginFragment)
    fun inject(fragment: DetailedPlaceFragment)
    fun inject(fragment: FavoritesFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(appRepository: AppRepository)

}

class AppUtils : Application() {

    override fun onCreate() {
        super.onCreate()
        Injector.applicationComponent =
            DaggerApplicationComponent.builder()
                .placesModule(PlacesModule(this))
                .build()
    }

    fun getPixelsFromDp(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}

/**
 * @return a [LatLng] object from the [Location] object
 */
fun Location.toLatLng() = LatLng(this.latitude, this.longitude)

