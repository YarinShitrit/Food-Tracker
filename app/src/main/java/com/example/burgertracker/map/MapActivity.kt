package com.example.burgertracker.map

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.Window
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.burgertracker.R
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.databinding.MapActivityBinding
import com.example.burgertracker.databinding.NavHeaderBinding
import com.example.burgertracker.favorites.FavoritesFragment
import com.example.burgertracker.login.LoginFragment
import com.example.burgertracker.settings.SettingsFragment
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.GoogleApiAvailability
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
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        Injector.applicationComponent.inject(this)
        mapViewModel = ViewModelProvider(this, mapViewModelFactory).get(MapViewModel::class.java)
        mapViewModel.appKey = resources.getString(R.string.google_maps_key)
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onPrepareOptionsMenu() called")
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onCreateOptionsMenu() called")
        //binding.toolbar.inflateMenu(R.menu.toolbar_menu)
        return super.onCreateOptionsMenu(menu)
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
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_to_settingsFragment)
                    binding.drawerLayout.close()
                }
                R.id.favItem -> {
                    Log.d(TAG, " favorites item clicked")
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_to_favoritesFragment)
                    binding.drawerLayout.close()
                }
            }
            return@setNavigationItemSelectedListener true
        }
        mapViewModel.currentFragment.observe(this, {
            when (it) {
                LoginFragment::class.java.name -> {
                    binding.toolbar.isVisible = false
                    binding.toolbar.isEnabled = false
                    binding.toggle.isDrawerIndicatorEnabled =
                        false // disables the DrawerMenuButton when LoginFragment is visible
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)//Locks the drawer menu from being opened
                }
                else -> {
                    when (it) {
                        MapFragment::class.java.name -> {
                            binding.navView.setCheckedItem(R.id.mapItem)
                        }
                        DetailedPlaceFragment::class.java.name -> {
                            binding.navView.setCheckedItem(R.id.mapItem)
                        }
                        SettingsFragment::class.java.name -> {
                            binding.navView.setCheckedItem(R.id.settingsItem)
                        }
                        FavoritesFragment::class.java.name -> {
                            binding.navView.setCheckedItem(R.id.favItem)
                        }
                    }
                    binding.toolbar.isVisible = true
                    binding.toolbar.isEnabled = true
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





