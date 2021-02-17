package com.example.burgertracker.favorites

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.burgertracker.databinding.FavListItemBinding
import com.example.burgertracker.placesData.Place

interface FavoriteItemClickListener {
    fun onClick(place: Place, position: Int)
}

private const val TAG = "FavListAdapter"

class FavListAdapter :
    RecyclerView.Adapter<FavListAdapter.FavoriteViewHolder>() {
    private lateinit var binding: FavListItemBinding
    private lateinit var favClickListener: FavoriteItemClickListener
    private var placesList: ArrayList<Place>? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        binding = FavListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        binding.placeName.text = placesList?.get(position)?.name
        binding.placeDistance.text = "Distance: ${placesList!![position].distance}km"
        binding.delButton.setOnClickListener {
            Log.d(TAG, "list is $placesList")
            Log.d(TAG, "POSITION IS $position")
            Log.d(TAG, "holder position is ${holder.adapterPosition}")
            favClickListener.onClick(placesList!![holder.adapterPosition], holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return if (placesList != null)
            placesList!!.size
        else
            0
    }

    fun setDeleteClickListener(listener: FavoriteItemClickListener) {
        favClickListener = listener
    }

    fun deletePlace(position: Int) {
        Log.d(TAG, "list before remove is $placesList")
        Log.d(TAG, "position remove is $position")
        placesList!!.removeAt(position)
        notifyItemChanged(position)
        notifyItemRangeRemoved(position, 1)
        Log.d(TAG, "list after remove is $placesList")
    }

    fun setData(it: List<Place>?) {
        if (it != null) {
            placesList = ArrayList(it)
            Log.d(TAG, "places are $placesList")
            notifyDataSetChanged()
        }
    }

    class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view)

}