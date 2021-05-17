package com.example.burgertracker.map

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.burgertracker.AppUtils
import com.example.burgertracker.R
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.databinding.FragmentMapBinding
import com.example.burgertracker.databinding.InfoWindowBinding
import com.example.burgertracker.models.OnInfoWindowElemTouchListener
import com.example.burgertracker.models.PlaceInfoWindow
import com.example.burgertracker.placesData.Place
import com.example.burgertracker.toLatLng
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.properties.Delegates


private const val TAG = "MapFragment"
private const val PERMISSION_ID = 10
private const val normalZoom = 14.0F

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    val binding get() = _binding!!
    private lateinit var infoWindowBinding: InfoWindowBinding
    private val gpsIOScope: CoroutineScope by lazy { CoroutineScope(Dispatchers.IO) }

    @Inject
    internal lateinit var mapViewModelFactory: MapViewModelFactory
    private val mapViewModel: MapViewModel by viewModels({ activity as MapActivity }) { mapViewModelFactory }
    private var permissionsResultFlag = false
    private val permissionsNeeded = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET
    )
    private lateinit var infoButtonClickListener: OnInfoWindowElemTouchListener
    private lateinit var infoWindow: PlaceInfoWindow
    private var gpsSnackBar: Snackbar? = null
    private var locationFlag = false
    private var searchRadius by Delegates.notNull<Int>()

    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        Log.d(TAG, "onCreate() called")
        Injector.applicationComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        infoWindowBinding = InfoWindowBinding.inflate(layoutInflater)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        mapViewModel.currentFragment.value = this::class.java.name
        searchRadius =
            PreferenceManager.getDefaultSharedPreferences(requireContext()).getInt("radius", 10)
        /**checks if
         * no permissions were granted even after requestPermissions() was called from onMapReady() so the user
         * denied the permissionsRequest and now need to display snackBar
         */
        if (!checkPermissions() && mapViewModel.isMapAvailable) {
            showPermissionsSnackBar()
        }
    }

    override fun onActivityCreated(p0: Bundle?) {
        super.onActivityCreated(p0)
        Log.d(TAG, "onActivityCreated() called")

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        if (checkPermissions()) {
            getCurrentLocation(requireActivity())
            if (mapViewModel.appMap.value != null) {
                updateUI()
                if (!mapViewModel.placesList.value.isNullOrEmpty()) {
                    mapViewModel.placesList.value!!.forEach {
                        Log.d(TAG, "Calling displayPlaces from onResume with $it")
                        displayPlaces(it)
                    }
                }
            }
        } else if (permissionsResultFlag) {
            showPermissionsSnackBar()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(p0: GoogleMap) {
        Log.d(TAG, "onMapReady() called")
        mapViewModel.appMap.value = p0
        mapViewModel.isMapAvailable = true
        if (!checkPermissions()) {
            requestPermissions(
                permissionsNeeded,
                PERMISSION_ID
            )
        } else {
            //Permissions Granted
            updateUI()
            if (!mapViewModel.placesList.value.isNullOrEmpty()) {
                mapViewModel.placesList.value!!.forEach { displayPlaces(it) }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
        mapViewModel.appMap.value?.clear()
        locationFlag = false
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult() called")
        permissionsResultFlag = true
        if (grantResults.all {
                it == PackageManager.PERMISSION_GRANTED
            }) {
            Log.d(TAG, "Permissions granted")
        } else {
            showPermissionsSnackBar()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkPermissions() = permissionsNeeded.all {
        checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateUI() {
        initMap()
        initSearchView()
        initObservers()
        initMarkersOnClick()
        initFoodTypeRecyclerView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initMap() {
        Log.d(TAG, "initMap called")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        //mapViewModel.appMap.value!!.mapType = GoogleMap.MAP_TYPE_HYBRID
        mapViewModel.appMap.value!!.uiSettings.isMyLocationButtonEnabled = true
        mapViewModel.appMap.value!!.uiSettings.isCompassEnabled = true
        mapViewModel.appMap.value!!.isMyLocationEnabled = true
        mapViewModel.appMap.value!!.uiSettings.isZoomControlsEnabled = true
        mapViewModel.appMap.value!!.setOnMyLocationClickListener {
            mapViewModel.appMap.value!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it.toLatLng(), mapViewModel.appMap.value!!.cameraPosition.zoom
                )
            )
        }
        infoButtonClickListener =
            object : OnInfoWindowElemTouchListener(infoWindowBinding.infoButton) {
                override fun onClickConfirmed(v: View?, marker: Marker?) {
                    Log.d(TAG, "info button Clicked")
                    findNavController().navigate(
                        R.id.action_mapFragment_to_detailedFragment,
                        null,
                        null,
                        null
                    )
                }
            }
        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        binding.mapWrapperLayout.init(
            mapViewModel.appMap.value!!,
            AppUtils().getPixelsFromDp(requireContext(), 39 + 20.toFloat())
        )
        infoWindow =
            PlaceInfoWindow(
                infoWindowBinding,
                binding.mapWrapperLayout,
                infoButtonClickListener
            )
        mapViewModel.appMap.value!!.setInfoWindowAdapter(infoWindow)
        infoWindowBinding.infoButton.setOnTouchListener(infoButtonClickListener)
    }

    /**
     * Gets the user current location and updates [MapViewModel.userLocation] accordingly
     * @param activity The activity that calls the method, Necessary for [LocationServices.getFusedLocationProviderClient]
     */
    private fun getCurrentLocation(activity: FragmentActivity) {
        Log.d(TAG, "getCurrentLocation called")
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            &&
            (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            try {
                //Try to get location from LocationManager
                val locationManager =
                    activity.getSystemService(Application.LOCATION_SERVICE) as LocationManager
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //Try to get location from LocationServices
                    var currentLocationTask =
                        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
                    currentLocationTask.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            Log.d(
                                TAG,
                                "Received Location from LocationServices -> Current location is ${location.toLatLng()}"
                            )
                            gpsSnackBar?.setText("Connected!")
                            if (mapViewModel.userLocation.value != location.toLatLng()) { //checking if userLocation is already the same to ignore duplications
                                mapViewModel.userLocation.value = location.toLatLng()
                            }
                            currentLocationTask = null
                        } else {
                            val locationGPS: Location? =
                                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (locationGPS != null) {
                                Log.d(
                                    TAG,
                                    "Received location from LocationManager -> Current location is ${locationGPS.toLatLng()}"
                                )
                                gpsSnackBar?.setText("Connected!")
                                if (mapViewModel.userLocation.value != locationGPS.toLatLng()) {
                                    mapViewModel.userLocation.value = locationGPS.toLatLng()
                                }
                            } else {
                                Log.d(TAG, "Unable to get current location")
                                if (gpsSnackBar != null) {
                                    gpsSnackBar?.setText("Unable to get location, Retrying to connect...")
                                        ?.show()
                                } else {
                                    gpsSnackBar = Snackbar.make(
                                        binding.root,
                                        "Unable to get location, Retrying to connect...",
                                        Snackbar.LENGTH_LONG
                                    ).apply {
                                        setBackgroundTint(
                                            ResourcesCompat.getColor(
                                                resources,
                                                R.color.colorPrimary,
                                                null
                                            )
                                        )
                                        show()
                                    }
                                }
                                gpsIOScope.launch {
                                    delay(2000)
                                    withContext(Dispatchers.Main) {
                                        getCurrentLocation(activity)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "GPS not available")
                    if (gpsSnackBar != null) {
                        gpsSnackBar?.setText("GPS is not available")?.show()
                    } else {
                        gpsSnackBar = Snackbar.make(
                            binding.root,
                            "GPS is not available",
                            Snackbar.LENGTH_LONG
                        ).apply {
                            setBackgroundTint(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.colorPrimary,
                                    null
                                )
                            )
                            show()
                        }
                    }
                    gpsIOScope.launch {
                        delay(2000)
                        withContext(Dispatchers.Main) {
                            getCurrentLocation(activity)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Could not receive current location -> ${e.localizedMessage}")
                gpsIOScope.launch {
                    delay(2000)
                    withContext(Dispatchers.Main) {
                        getCurrentLocation(activity)
                    }
                }
            }
        }
    }

    /**
     * Display all places from placesList on the map
     */
    private fun displayPlaces(place: Place) {
        Log.d(TAG, "displayPlaces() called")
        val markerOptions = MarkerOptions()
            .position(LatLng(place.geometry.location.lat, place.geometry.location.lng))
            .title(place.name)
            .icon(place.markerIcon)
        mapViewModel.appMap.value!!.addMarker(markerOptions)
    }

    /**
     * Initializes the RecyclerView and [FoodListAdapter]
     */
    private fun initFoodTypeRecyclerView() {
        Log.d(TAG, "initFoodRecyclerView() called")
        binding.foodTypeList.layoutManager =
            LinearLayoutManager(requireContext()).apply {
                orientation = RecyclerView.HORIZONTAL
            }
        val adapter = FoodListAdapter()
            .apply {
                setData(
                    arrayListOf(
                        "All",
                        "Pizza",
                        "Sushi",
                        "Burger",
                        "Mexican",
                        "Italian",
                        "Coffee"
                    )
                )
            }
        adapter.setClickListener(object : ViewHolderClickListener {
            override fun click() {
                Log.d(TAG, "item clicked")
                mapViewModel.placesList.value?.clear()
                mapViewModel.appMap.value!!.clear()
                if (adapter.itemClicked == "All") {
                    mapViewModel.queryIcon.value = null
                    mapViewModel.getNearbyPlaces(null, searchRadius)
                } else {
                    mapViewModel.queryIcon.value = adapter.itemClicked
                    mapViewModel.getNearbyPlaces(adapter.itemClicked, searchRadius)
                }
            }
        })
        binding.foodTypeList.adapter = adapter
    }

    private fun initSearchView() {
        Log.d(TAG, "initSearchView() called")
        binding.mapSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d(TAG, " calling getNearbyPlaces from query")
                mapViewModel.placesList.value?.clear()
                mapViewModel.appMap.value!!.clear()
                mapViewModel.getNearbyPlaces(query, searchRadius)
                binding.mapSearch.clearFocus() //closes the keyboard after performing the search
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun initObservers() {
        Log.d(TAG, "initObservers called")
        mapViewModel.userLocation.observe(this, {
            Log.d(TAG, "userLocation observer triggered -> $it")
            //initMap()
            mapViewModel.appMap.value!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    it, normalZoom
                )
            )
            mapViewModel.appMap.value!!.setOnMyLocationButtonClickListener {
                mapViewModel.appMap.value!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        it, normalZoom
                    )
                )
                true
            }
            if (!locationFlag) {
                locationFlag = true
                if (mapViewModel.placesList.value.isNullOrEmpty()) {
                    Log.d(TAG, "calling getNearbyPlaces() from placesList observer")
                    mapViewModel.getNearbyPlaces(null, searchRadius)
                }
            }
        })
        mapViewModel.mediatorPlace.observe(this, {
            Log.d(TAG, "Mediator observer triggered -> calling displayPlaces() with $it")
            displayPlaces(it)
        })

        mapViewModel.currentUserPhoto.observe(this, {
            Log.d(TAG, "currentUserPhoto observer triggered -> displaying photo $it")
            (requireActivity() as MapActivity).binding.navView.getHeaderView(0)
                .findViewById<ImageView>(R.id.userPhoto)
                .setImageBitmap(it)
        })

        mapViewModel.currentUser.observe(this, {
            if (it != null) {
                Log.d(
                    TAG,
                    "currentUser observer triggered -> displaying user ${it.displayName}"
                )
                (requireActivity() as MapActivity).binding.navView.getHeaderView(0)
                    .findViewById<TextView>(R.id.userName).text = it.displayName
                (requireActivity() as MapActivity).binding.navView.getHeaderView(0)
                    .findViewById<TextView>(R.id.userEmail).text = it.email
            }
        })
    }

    private fun initMarkersOnClick() {
        Log.d(TAG, "initMarkersOnClick() called")
        mapViewModel.appMap.value?.setOnMarkerClickListener {
            val currentFocusedPlace = mapViewModel.placesList.value?.find { place ->
                LatLng(place.geometry.location.lat, place.geometry.location.lng) == it.position
            }?.also {

                Log.d(TAG, "Marker clicked, place is ${it.name}")
                runBlocking {
                    /*
                    runs Async to get if place is in favorites before displaying its infoWindow on the map
                     */
                    val ifPlaceIsFavorite = async(Dispatchers.IO) {
                        withContext(Dispatchers.Default) {
                            mapViewModel.getIfPlaceIsFavorite(it)
                        }
                    }
                    if (ifPlaceIsFavorite.await() != null) {
                        Log.d(TAG, "place is in favorites")
                        setInFavoriteButtonImage()
                        it.isLiked = true
                    } else {
                        Log.d(TAG, "place is not in favorites")
                        setNotInFavoriteButtonImage()
                    }
                    infoWindow.setPlace(it)
                }
            }
            mapViewModel.currentFocusedPlace.value = currentFocusedPlace
            if (currentFocusedPlace != null) {
                mapViewModel.appMap.value!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            currentFocusedPlace.geometry.location.lat,
                            currentFocusedPlace.geometry.location.lng
                        ), mapViewModel.appMap.value!!.cameraPosition.zoom
                    )
                )
            }
            false
        }
    }

    private fun setNotInFavoriteButtonImage() {
        Log.d(TAG, "setNotInFavoriteButtonImage() called")
        infoWindowBinding.like.crossfade = 0F
        infoWindowBinding.like.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gmm_white,
                null
            )
        )
    }

    private fun setInFavoriteButtonImage() {
        Log.d(TAG, "setFilledLikeButtonImage() called, infoWindow is ${infoWindow.hashCode()}")
        infoWindowBinding.like.crossfade = 1F
        infoWindowBinding.like.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.gmm_white,
                null
            )
        )
    }

    private fun showPermissionsSnackBar() {
        Log.d(TAG, "showPermissionsSnackBar() called")
        Snackbar.make(
            binding.mapWrapperLayout,
            "Please Enable Location Permission",
            Snackbar.LENGTH_INDEFINITE
        ).setAction(
            "Enable Location"
        ) {
            val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            requireActivity().startActivityForResult(settingsIntent, 1)
        }
            .show()
    }
}