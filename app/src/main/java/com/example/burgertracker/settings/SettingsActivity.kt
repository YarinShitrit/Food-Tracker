package com.example.burgertracker.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.example.burgertracker.R

private const val TAG = "SettingsActivity"

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
            preferenceManager.sharedPreferencesName = "prefs"
            Log.d(TAG, preferenceManager.sharedPreferencesName)
            preferenceScreen.findPreference<SeekBarPreference>("range")?.value =
                preferenceManager.sharedPreferences.getInt("range", 25)
            preferenceScreen.findPreference<SeekBarPreference>("range")
                ?.setOnPreferenceChangeListener { preference, newValue ->
                    Log.d(TAG, "seekBar value changed new values is $newValue")
                    preferenceManager.sharedPreferences.edit().putInt("range", newValue as Int)
                        .apply()
                    (preference as SeekBarPreference).setDefaultValue(newValue)
                    return@setOnPreferenceChangeListener true
                }

        }
    }
}