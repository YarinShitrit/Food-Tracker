package com.example.burgertracker.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.viewbinding.ViewBinding
import com.example.burgertracker.R
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.databinding.MapActivityBinding
import com.example.burgertracker.databinding.NavHeaderBinding
import com.example.burgertracker.login.LoginFragment
import com.firebase.ui.auth.AuthUI
import javax.inject.Inject

private const val TAG = "MapActivity"


class MapActivity : AppCompatActivity() {
    lateinit var binding: MapActivityBinding
    private lateinit var navHeaderBinding: NavHeaderBinding

    @Inject
    internal lateinit var mapViewModelFactory: MapViewModelFactory
    private lateinit var mapViewModel: MapViewModel
    private val MapActivityBinding.toggle: ActionBarDrawerToggle by lazy { setToggle() }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate() called")
        super.onCreate(savedInstanceState)
        Injector.applicationComponent.inject(this)
        mapViewModel = ViewModelProvider(this, mapViewModelFactory).get(MapViewModel::class.java)
        mapViewModel.appKey = resources.getString(R.string.google_maps_key)
        Log.d(TAG, "ViewModel is ${mapViewModel.hashCode()}")
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = MapActivityBinding.inflate(layoutInflater)
        navHeaderBinding = NavHeaderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar).also {
            title = null
        }
        initDrawerAndNavigation()
    }

    override fun onStart() {
        Log.d(TAG, "onStart() called")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume() called")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause() called")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop() called")
        super.onStop()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onPostCreate() called")
        super.onPostCreate(savedInstanceState)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy() called")
        super.onDestroy()
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
     * Initializes the DrawerLayout and side navigation
     */
    private fun initDrawerAndNavigation() {
        binding.logout.setOnClickListener {
            mapViewModel.appMap.value?.clear()
            mapViewModel.placesList.value?.clear()
            mapViewModel.currentUser.value = null
            mapViewModel.currentUserPhoto.value = null
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_to_loginFragment)
                }
            binding.drawerLayout.close()
        }
        binding.navView.setNavigationItemSelectedListener {
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
        mapViewModel.currentFragment.observe(this, {
            when (it) {
                LoginFragment::class.java.name -> {
                    binding.toggle.isDrawerIndicatorEnabled =
                        false // disables the DrawerMenuButton when LoginFragment is visible
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)//Locks the drawer menu from being opened
                }
                else -> {
                    binding.toggle.isDrawerIndicatorEnabled = true
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)//Unlocks the drawer menu
                }
            }
        })
    }

    private fun setToggle(): ActionBarDrawerToggle {
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        toggle.syncState()
        return toggle
    }


}





