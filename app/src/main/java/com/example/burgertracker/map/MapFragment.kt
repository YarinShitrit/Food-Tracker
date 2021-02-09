package com.example.burgertracker.map

import android.Manifest
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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
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
import javax.inject.Inject


private const val TAG = "MapFragment"
private const val PERMISSION_ID = 10
private const val normalZoom = 14.0F

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    val binding get() = _binding!!

    @Inject
    internal lateinit var mapViewModelFactory: MapViewModelFactory
    private val mapViewModel: MapViewModel by viewModels({ activity as MapActivity }) { mapViewModelFactory }
    private var permissionsResultFlag = false
    private val permissionsNeeded = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.INTERNET
    )
    private lateinit var likeImageButtonClickListener: OnInfoWindowElemTouchListener
    private lateinit var infoWindow: PlaceInfoWindow

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
        Log.d(TAG, "ViewModel is ${mapViewModel.hashCode()}")
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        if (checkPermissions()) {
            if (mapViewModel.appMap.value != null) {
                getCurrentLocation(requireActivity())
                initFoodTypeRecyclerView()
                initMap()
                initMarkersOnClick()
                initObservers()
                initSearchView()
                if (!mapViewModel.placesList.value.isNullOrEmpty()) {
                    displayPlaces(mapViewModel.placesList.value!!)
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
            initMap()
            initSearchView()
            initObservers()
            initMarkersOnClick()
            initFoodTypeRecyclerView()
            getCurrentLocation(requireActivity())
            if (!mapViewModel.placesList.value.isNullOrEmpty()) {
                displayPlaces(mapViewModel.placesList.value!!)
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
        val infoWindowBinding = InfoWindowBinding.inflate(layoutInflater)
        likeImageButtonClickListener =
            object : OnInfoWindowElemTouchListener(infoWindowBinding.like) {
                override fun onClickConfirmed(v: View?, marker: Marker?) {
                    Log.d(TAG, "like clicked")
                    val likeButton = v as ImageButton
                    Toast.makeText(
                        requireContext(),
                        "${infoWindowBinding.placeName.text} added to favorites ",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG,"inserting ${mapViewModel.currentFocusedPlace.value!!}")
                    mapViewModel.addPlaceToFavorites(mapViewModel.currentFocusedPlace.value!!)
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
                likeImageButtonClickListener
            )
        mapViewModel.appMap.value!!.setInfoWindowAdapter(infoWindow)
        infoWindowBinding.like.setOnTouchListener(likeImageButtonClickListener)
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
                val nManager =
                    activity.getSystemService(Application.LOCATION_SERVICE) as LocationManager
                if (nManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    val locationGPS: Location? =
                        nManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (locationGPS != null) {
                        Log.d(
                            TAG,
                            "Received location from LocationManager -> Current location is ${locationGPS.toLatLng()}"
                        )
                        mapViewModel.userLocation.value = locationGPS.toLatLng()
                    } else {
                        //Try to get location from LocationServices
                        val currentLocationTask =
                            LocationServices.getFusedLocationProviderClient(activity).lastLocation
                        currentLocationTask.addOnSuccessListener(activity) {
                            if (currentLocationTask.isSuccessful) {
                                Log.d(
                                    TAG,
                                    "Received Location from LocationServices -> Current location is ${it.toLatLng()}"
                                )
                                mapViewModel.userLocation.value = it.toLatLng()
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "GPS not available")
                    Toast.makeText(activity, "Turn on GPS", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.d(TAG, "Could not receive current location -> ${e.localizedMessage}")
            }
        }
    }

    /**
     * Display all places from placesList on the map
     */
    private fun displayPlaces(placesList: ArrayList<Place>) {
        Log.d(TAG, "displayPlaces() called")
        for (place in placesList) {
            val markerOptions = MarkerOptions()
                .position(LatLng(place.geometry.location.lat, place.geometry.location.lng))
                .title(place.name)
                .icon(place.markerIcon)
            mapViewModel.appMap.value!!.addMarker(markerOptions)
        }

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
                mapViewModel.queryIcon.value = adapter.itemClicked
                mapViewModel.getNearbyPlaces(adapter.itemClicked)

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
                mapViewModel.getNearbyPlaces(query)
                //clearFocus() closes the keyboard after performing the search
                binding.mapSearch.clearFocus()
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
            if (mapViewModel.placesList.value.isNullOrEmpty()) {
                Log.d(TAG, "calling getNearbyPlaces() from placesList observer")
                mapViewModel.getNearbyPlaces(null)
            }
        })
        mapViewModel.mediator.observe(this, {
            Log.d(TAG, "Mediator observer triggered -> calling displayPlaces()")
            mapViewModel.setPlacesDistance(it)
            mapViewModel.setPlacesMarkerIcon(it)
            displayPlaces(it)
        })
        mapViewModel.placesList.observe(this, {
            if (!it.isNullOrEmpty()) {
                Log.d(TAG, "placesList observer triggered -> calling displayPlaces()")
                displayPlaces(it)
            }
        })
        mapViewModel.currentUserPhoto.observe(this, {
            Log.d(TAG, "currentUserPhoto observer triggered -> displaying photo $it")
            (requireActivity() as MapActivity).binding.navView.getHeaderView(0)
                .findViewById<ImageView>(R.id.userPhoto)
                .setImageBitmap(it)
        })

        mapViewModel.currentUser.observe(this, {
            if (it != null) {
                Log.d(TAG, "currentUser observer triggered -> displaying user ${it.displayName}")
                (requireActivity() as MapActivity).binding.navView.getHeaderView(0)
                    .findViewById<TextView>(R.id.userName).text = it.displayName
                (requireActivity() as MapActivity).binding.navView.getHeaderView(0)
                    .findViewById<TextView>(R.id.userEmail).text = it.email
            }
        })
    }

    private fun initMarkersOnClick() {
        Log.d(TAG, "enableMarkersOnClick called")
        mapViewModel.appMap.value!!.setOnMarkerClickListener {
            val place = mapViewModel.placesList.value?.find { place ->
                LatLng(place.geometry.location.lat, place.geometry.location.lng) == it.position
            }!!.also {
                mapViewModel.currentFocusedPlace.value = it
            }
            infoWindow.setPlace(place)
            // if (mapViewModel.isPlaceInFavorites(place)){
            //TODO display liked image
            //   }
            mapViewModel.appMap.value!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        place.geometry.location.lat,
                        place.geometry.location.lng
                    ), mapViewModel.appMap.value!!.cameraPosition.zoom
                )
            )
            false
        }
    }

    private fun showPermissionsSnackBar() {
        Log.d(TAG, "showPermissionsSnackBar called")
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