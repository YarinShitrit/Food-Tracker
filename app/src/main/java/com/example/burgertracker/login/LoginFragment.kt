package com.example.burgertracker.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.burgertracker.R
import com.example.burgertracker.databinding.FragmentLoginBinding
import com.example.burgertracker.map.MapViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

private const val TAG = "LoginFragment"
private const val RC_SIGN_IN = 123

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var mapViewModel: MapViewModel
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called")
        super.onViewCreated(view, savedInstanceState)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Log.d(
                TAG,
                "User ${auth.currentUser!!.displayName} already logged in -> navigating to MapFragment "
            )
            fetchUserData(auth.currentUser!!)
            findNavController().navigate(R.id.action_loginFragment_to_mapsFragment)
            onDestroyView()
        } else {
            auth.setLanguageCode("en")
            binding.sendSms.setOnClickListener {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(binding.phoneNumberEditText.text.toString()) // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(requireActivity()) // Activity (for callback binding)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                            Log.d(TAG, "Verification completed ")
                            signInWithPhoneAuthCredential(p0)
                        }

                        override fun onVerificationFailed(p0: FirebaseException) {
                            Log.e(TAG, "Verification failed -> ${p0.localizedMessage}")
                        }

                        override fun onCodeSent(
                            p0: String,
                            p1: PhoneAuthProvider.ForceResendingToken
                        ) {
                            super.onCodeSent(p0, p1)
                            Log.d(TAG, "Verification code sent")
                        }

                        override fun onCodeAutoRetrievalTimeOut(p0: String) {
                            super.onCodeAutoRetrievalTimeOut(p0)
                            Log.d(TAG, "Verification code timeout")
                        }
                    }) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)

            }
            binding.googleLoginBtn.setOnClickListener {
                signInWithFirebaseProvider()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                Log.i(TAG, "Login Successfully -> current user is $user")
                if (user != null) {
                    fetchUserData(user)
                    findNavController().navigate(R.id.action_loginFragment_to_mapsFragment)
                }
            } else {
                Log.e(TAG, "Login Failed -> ${response?.error}")
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signInWithFirebaseProvider() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            RC_SIGN_IN
        )

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result.user
                    if (user != null)
                        fetchUserData(user)
                    findNavController().navigate(R.id.action_loginFragment_to_mapsFragment)
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.e(TAG, "Verification code invalid")
                    }
                }
            }
    }

    private fun fetchUserData(user: FirebaseUser) {
        mapViewModel.downloadCurrentUserPhoto(user)
        mapViewModel.currentUser.value = user
    }
}