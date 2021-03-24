package com.example.burgertracker.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.burgertracker.R
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.databinding.FragmentLoginBinding
import com.example.burgertracker.map.MapActivity
import com.example.burgertracker.map.MapViewModel
import com.example.burgertracker.map.MapViewModelFactory
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import javax.inject.Inject


private const val TAG = "LoginFragment"
private const val GOOGLE_SIGN_IN = 123

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var mapViewModelFactory: MapViewModelFactory
    private val mapViewModel: MapViewModel by viewModels({ activity as MapActivity }) { mapViewModelFactory }
    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var auth: FirebaseAuth


    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        Log.d(TAG, "onCreate() called")
        Injector.applicationComponent.inject(this)
        setUpFacebookLogin()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated() called")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated() called")
        super.onViewCreated(view, savedInstanceState)
        mapViewModel.currentFragment.value = this::class.java.name
        mapViewModel.deleteAllPlacesLocally()
        mapViewModel.placesList.value?.clear()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Log.d(
                TAG,
                "User ${auth.currentUser!!.displayName} already logged in -> navigating to MapFragment "
            )
            initUserData(auth.currentUser!!)
            findNavController().navigate(R.id.action_loginFragment_to_mapsFragment)
            onDestroyView()
        } else {
            //setPhoneAuth()
            binding.googleLoginBtn.setOnClickListener {
                signInWithGoogle()
            }
            binding.facebookLoginBtn.setOnClickListener {
                signInWithFacebook()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.e(TAG, "Google sign in failed", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "firebaseAuthWithGoogle() called")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val firebaseUser = task.result.user
                    if (firebaseUser != null) {
                        initUserData(firebaseUser)
                        if (task.result.additionalUserInfo!!.isNewUser) {
                            Log.d(TAG, "Creating new user to database")
                            mapViewModel.createNewUser()
                        }
                        findNavController().navigate(R.id.action_loginFragment_to_mapsFragment)
                    }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Log.d(TAG, "duplicate user")
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.google_login_duplicate_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(
                        binding.root, "Authentication failed.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun setUpFacebookLogin() {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d(TAG, "Facebook Login success")
                    if (loginResult != null) {
                        firebaseAuthWithFacebook(loginResult.accessToken)
                    }
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook Login cancelled")
                }

                override fun onError(exception: FacebookException) {
                    Log.d(TAG, "Facebook Login failed, ${exception.localizedMessage}")
                }
            })
    }

    private fun signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(
            this,
            listOf("public_profile", "email", "user_friends")
        )
    }

    private fun firebaseAuthWithFacebook(token: AccessToken) {
        Log.d(TAG, "firebaseAuthWithFacebook() called -> facebook token is $token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result.user
                    if (user != null) {
                        if (task.result.additionalUserInfo!!.isNewUser) {
                            Log.d(TAG, "Creating new user to database")
                            mapViewModel.createNewUser(token.token)
                        }
                        initUserData(user, token.token)
                        findNavController().navigate(R.id.action_loginFragment_to_mapsFragment)
                    }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Log.d(TAG, "duplicate user")
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.facebook_login_duplicate_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(
                        binding.root, "Authentication failed.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
    }

  /*  private fun signInWithPhone(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result.user
                    if (user != null) {
                        initUserData(user)
                        findNavController().navigate(R.id.action_loginFragment_to_mapsFragment)
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Log.e(TAG, "Verification code invalid")
                        }
                    }
                }
            }
    }*/

   /* private fun setPhoneAuth() {
        auth.setLanguageCode("en")
        binding.sendSms.setOnClickListener {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(binding.phoneNumberEditText.text.toString()) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity()) // Activity (for callback binding)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                        Log.d(TAG, "Verification completed ")
                        signInWithPhone(p0)
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
    }*/

    private fun initUserData(user: FirebaseUser, fbToken: String? = null) {
        mapViewModel.initUserData(user, fbToken)
    }
}