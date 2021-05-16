package com.example.burgertracker.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.burgertracker.R
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.databinding.FragmentFavoritesBinding
import com.example.burgertracker.map.MapActivity
import com.example.burgertracker.map.MapViewModel
import com.example.burgertracker.map.MapViewModelFactory
import com.example.burgertracker.placesData.Place
import javax.inject.Inject

private const val TAG = "FavoritesFragment"

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    @Inject
    internal lateinit var mapViewModelFactory: MapViewModelFactory
    private val mapViewModel: MapViewModel by viewModels({ activity as MapActivity }) { mapViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
        Injector.applicationComponent.inject(this)
        mapViewModel.getAllPlacesByDistance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentFavoritesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        mapViewModel.currentFragment.value = this::class.java.name
        initFavoritesRecyclerView()
        initListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
        _binding = null
        mapViewModel.favPlaces.removeObservers(requireActivity())
    }

    private fun initListeners() {
        binding.delAllButton.setOnClickListener {
            if (!mapViewModel.favPlaces.value.isNullOrEmpty()) {
                Log.d(TAG, mapViewModel.favPlaces.value.toString())
                val alertDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Delete all favorites?").setIcon(
                        ResourcesCompat.getDrawable(
                            resources,
                            R.drawable.ic_baseline_delete_24,
                            null
                        )
                    )
                    .setPositiveButton(
                        "Delete"
                    ) { _, _ ->
                        mapViewModel.deleteAllPlaces()
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                    .create()
                alertDialog.show()
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
                )
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ResourcesCompat.getColor(resources, R.color.buttonColor, null)
                )

            } else {
                Toast.makeText(requireContext(), "List is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initFavoritesRecyclerView() {
        mapViewModel.favPlaces.observe(requireActivity(), {
            Log.d(TAG, "places added to list $it")
            val adapter = FavListAdapter(it)
            adapter.setDeleteClickListener(object : FavoriteItemClickListener {
                override fun onClick(place: Place, position: Int) {
                    Log.d(TAG, "Favorite remove clicked - ${place.name}")
                    mapViewModel.removePlaceFromFavorites(place)
                    Toast.makeText(
                        requireContext(),
                        "Removed ${place.name} from favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

            binding.favoritesRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            binding.favoritesRecyclerView.adapter = adapter
            (binding.favoritesRecyclerView.adapter as FavListAdapter).setData(mapViewModel.favPlaces.value)
        })

    }
}