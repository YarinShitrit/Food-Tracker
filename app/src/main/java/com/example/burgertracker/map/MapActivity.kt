package com.example.burgertracker.map


import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.burgertracker.R
import com.example.burgertracker.databinding.MapActivityBinding
import com.example.burgertracker.databinding.NavHeaderBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.info_window.view.*
import kotlinx.android.synthetic.main.map_activity.*
import kotlinx.android.synthetic.main.map_activity.view.*
import kotlinx.android.synthetic.main.nav_header.*
import kotlinx.android.synthetic.main.nav_header.view.*

private const val TAG = "MapActivity"

class MapActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mapViewModel: MapViewModel
    lateinit var binding: MapActivityBinding
    private lateinit var navHeaderBinding: NavHeaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = MapActivityBinding.inflate(layoutInflater)
        navHeaderBinding = NavHeaderBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(toolbar)
        initDrawerAndNavigation()
    }

    override fun onStart() {
        Log.d(TAG, "onStart called")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume called")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause called")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop called")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.mapItem -> {
                Log.d(TAG, " map clicked")
                findNavController(R.id.nav_controller_view_tag).navigate(R.id.action_to_mapFragment)
            }
            R.id.settingsItem -> {
                Log.d(TAG, " settings clicked")
                findNavController(R.id.nav_controller_view_tag).navigate(R.id.action_mapFragment_to_settingsFragment)
                return true
            }
        }
        return true
    }

    /**
     * checks if the navigation drawer is open and if so it will close it on back press otherwise close the activity
     */
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Initializes [binding.drawer_layout] and side navigation
     */
    private fun initDrawerAndNavigation() {
        binding.logout.setOnClickListener {
            mapViewModel.appMap.value?.clear()
            mapViewModel.placesList.value?.clear()
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_to_loginFragment)
                }
            binding.drawerLayout.close()
        }

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.mapItem -> {
                    Log.d(TAG, "map item clicked")
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_to_mapFragment)
                    binding.drawerLayout.close()
                }
                R.id.settingsItem -> {
                    Log.d(TAG, " settings item clicked")
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_mapFragment_to_settingsFragment)
                    binding.drawerLayout.close()
                }
            }
            return@setNavigationItemSelectedListener true
        }
        toggle.syncState()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /* if (savedInstanceState == null || navView.checkedItem?.itemId == R.id.mapItem) {
             val mapFragment = supportFragmentManager
                 .findFragmentById(R.id.map_fragment) as SupportMapFragment
             mapFragment.getMapAsync(this)
         }*/
    }
}




