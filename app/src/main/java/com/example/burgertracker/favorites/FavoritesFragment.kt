package com.example.burgertracker.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
        _binding = null
        mapViewModel.favPlaces.removeObservers(requireActivity())
    }

    private fun initFavoritesRecyclerView() {
        val adapter = FavListAdapter()
        adapter.setDeleteClickListener(object : FavoriteItemClickListener {
            override fun onClick(place: Place, position: Int) {
                Log.d(TAG, "Favorite remove clicked - ${place.name}")
                mapViewModel.removePlaceFromFavorites(place)
                Toast.makeText(
                    requireContext(),
                    "Removed ${place.name} from favorites",
                    Toast.LENGTH_SHORT
                ).show()
                adapter.deletePlace(position)
            }
        })
        binding.favoritesRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.favoritesRecyclerView.adapter = adapter
        mapViewModel.favPlaces.observe(requireActivity(), {
            (binding.favoritesRecyclerView.adapter as FavListAdapter).setData(it)
        })
        mapViewModel.getAllPlacesByDistance()
    }
}