package com.example.burgertracker.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.example.burgertracker.R
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "SettingsFragment"

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var auth: FirebaseAuth
    private val callbackManager = CallbackManager.Factory.create()
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