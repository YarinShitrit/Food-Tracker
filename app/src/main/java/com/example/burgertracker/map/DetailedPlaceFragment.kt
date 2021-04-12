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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.example.burgertracker.dagger.Injector
import com.example.burgertracker.databinding.FragmentDetailedPlaceBinding
import com.example.burgertracker.firebase.FCMServiceEvents
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
        Log.d(TAG, "onCreateView() called")
        _binding = FragmentDetailedPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")
        mapViewModel.currentFragment.value = this::class.java.name
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
        mapViewModel.addUserToPlaceCloudUpdates()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
        mapViewModel.removeUserFromPlaceCloudUpdates()//Removing FCM when fragment is not longer visible to user
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
        FCMServiceEvents.placeFavoritesLiveData.removeObservers(requireActivity())
        FCMServiceEvents.placeFavoritesLiveData.value = "0"
        mapViewModel.currentFocusedPlaceReviews.removeObservers(requireActivity())
        mapViewModel.currentFocusedPlaceReviews.value?.clear()
        _binding = null
    }

    private fun initReviewsList() {
        val adapter = ReviewsListAdapter()
        binding.reviewsList.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.reviewsList.adapter = adapter
        mapViewModel.currentFocusedPlaceReviews.observe(requireActivity(), {
            Log.d(TAG, "Reviews received from observer $it")
            if (it != null) {
                Log.d(TAG, binding.toString())
                (binding.reviewsList.adapter as ReviewsListAdapter).setData(it)
            }
        })
        mapViewModel.downloadDetailedPlaceReviews()
    }

    private fun updateUI() {
        val place = mapViewModel.currentFocusedPlace.value!!
        binding.placeName.text = place.name
        binding.placeAddress.text = place.vicinity
        binding.placeDistance.text = "Distance: ${place.distance}km"
        binding.placeRating.text =
            if (place.rating != null) "Rating: ${place.rating}" else "Rating: None"
        binding.placeFavorites.text = "${place.totalFavorites} people added it to favorites"
        setListeners()
        initObservers()
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
        initReviewsList()
    }

    private fun initObservers() {
        FCMServiceEvents.placeFavoritesLiveData.observe(requireActivity(), {
            Log.d(TAG, "Current Place favorites changed")
            binding.placeFavorites.text = "$it people added it to favorites"
        })
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