package com.example.burgertracker.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.transition.TransitionInflater
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.databinding.FragmentDetailedPlaceBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "DetailedPlaceFragment"

class DetailedPlaceFragment : Fragment() {
    private var _binding: FragmentDetailedPlaceBinding? = null
    private val binding get() = _binding!!
    private val ioSCOPE = CoroutineScope(Dispatchers.IO)

    @Inject
    internal lateinit var mapViewModelFactory: MapViewModelFactory
    private val mapViewModel: MapViewModel by viewModels({ activity as MapActivity }) { mapViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
        Injector.applicationComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailedPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        mapViewModel.currentFragment.value = this::class.java.name
        updateUI()
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
        _binding = null
    }

    private fun updateUI() {
        val place = mapViewModel.currentFocusedPlace.value!!
        binding.placeName.text = place.name
        binding.placeAddress.text = place.formatted_address
        binding.placeDistance.text = "Distance: ${place.distance}km"
        binding.placeRating.text = "Rating: ${place.rating}"
        ioSCOPE.launch {
            if (mapViewModel.getIfPlaceIsFavorite(place) != null) {
                withContext(Dispatchers.Main) {
                    binding.motionLayout.transitionToEnd()
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.motionLayout.transitionToStart()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.placeLikeButton.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                Log.d(TAG, "Like button clicked")
                binding.placeLikeButton.performClick()
                val place = mapViewModel.currentFocusedPlace.value!!
                ioSCOPE.launch {
                    if (mapViewModel.getIfPlaceIsFavorite(place) != null) {
                        Log.d(TAG, "Removing ${place.name} from favorites")
                        mapViewModel.removePlaceFromFavorites(place)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "${place.name} removed from favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        Log.d(TAG, "Adding ${place.name} to favorites")
                        mapViewModel.addPlaceToFavorites(place)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "${place.name} added to favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                true
            } else
                false
        }
    }
}