package com.example.burgertracker.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.example.burgertracker.R
import com.example.burgertracker.map.MapViewModel

private const val TAG = "SettingsActivity"
private lateinit var mapViewModel: MapViewModel

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(findViewById(R.id.tb))
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
            preferenceManager.sharedPreferencesName = "prefs"
            val seekBarRadius = preferenceScreen.findPreference<SeekBarPreference>("radius")
            seekBarRadius?.value= mapViewModel.searchRadius.value!!
            seekBarRadius?.setOnPreferenceChangeListener { _, newValue ->
                Log.d(TAG, "seekBar value changed, new value is $newValue")
                preferenceManager.sharedPreferences.edit().putInt("radius", newValue as Int)
                    .apply()
                mapViewModel.searchRadius.value = newValue
                return@setOnPreferenceChangeListener true
            }
        }
    }
}