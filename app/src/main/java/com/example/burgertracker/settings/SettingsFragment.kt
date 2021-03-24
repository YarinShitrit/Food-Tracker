package com.example.burgertracker.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.example.burgertracker.R
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.map.MapActivity
import com.example.burgertracker.map.MapViewModel
import com.example.burgertracker.map.MapViewModelFactory
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

private const val TAG = "SettingsFragment"

class SettingsFragment : PreferenceFragmentCompat() {
    @Inject
    internal lateinit var mapViewModelFactory: MapViewModelFactory
    private val mapViewModel: MapViewModel by viewModels({ activity as MapActivity }) { mapViewModelFactory }
    private lateinit var auth: FirebaseAuth
    private val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
        Injector.applicationComponent.inject(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        auth = FirebaseAuth.getInstance()
        preferenceManager.findPreference<SwitchPreferenceCompat>("fbLinking")
            ?.setOnPreferenceClickListener {
                if ((it as SwitchPreferenceCompat).isChecked) {
                    linkFacebookAccount()
                }
                true
            }
        preferenceManager.findPreference<SeekBarPreference>("radius")
            ?.setOnPreferenceChangeListener { _, newValue ->
                mapViewModel.searchRadius.value = newValue as Int
                true
            }
    }

    override fun onResume() {
        super.onResume()
        mapViewModel.currentFragment.value = this::class.java.name

    }

    private fun linkFacebookAccount() {
        val loginManager = LoginManager.getInstance()
        loginManager.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d(TAG, "Facebook Login success")
                    if (loginResult != null) {
                        val credential =
                            FacebookAuthProvider.getCredential(loginResult.accessToken.token)
                        auth.currentUser!!.linkWithCredential(credential)
                            .addOnCompleteListener(requireActivity()) { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "linkWithCredential:success")
                                    Toast.makeText(
                                        requireContext(),
                                        "Facebook Account Linked",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {
                                    Log.w(TAG, "linkWithCredential:failure", task.exception)
                                }
                            }
                    }
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook Login cancelled")
                }

                override fun onError(exception: FacebookException) {
                    Log.d(
                        TAG, "Facebook Login failed, ${exception.localizedMessage}"
                    )
                }
            })
        loginManager.logInWithReadPermissions(
            this,
            listOf("public_profile", "email", "user_friends")
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}