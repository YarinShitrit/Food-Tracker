package com.example.burgertracker.favorites

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.burgertracker.databinding.FavListItemBinding
import com.example.burgertracker.placesData.Place

interface FavoriteItemClickListener {
    fun onClick(place: Place, position: Int)
}

private const val TAG = "FavListAdapter"

class FavListAdapter(var placesList: ArrayList<Place>) :
    RecyclerView.Adapter<FavListAdapter.FavoriteViewHolder>() {
    private lateinit var binding: FavListItemBinding
    private lateinit var delClickListener: FavoriteItemClickListener
    private lateinit var favClickListener: FavoriteItemClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        binding = FavListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.binding.placeName.text = placesList.get(holder.adapterPosition).name
        holder.binding.placeDistance.text =
            "Distance: ${placesList[holder.adapterPosition].distance}km"
        holder.binding.root.setOnClickListener {
            favClickListener.onClick(placesList[holder.adapterPosition], holder.adapterPosition)
        }
        holder.binding.delButton.setOnClickListener {
            placesList.forEach { Log.d(TAG, "${placesList.indexOf(it)} -> ${it.name} \n") }
            Log.d(TAG, "POSITION IS $position")
            Log.d(TAG, "holder position is ${holder.adapterPosition}")
            delClickListener.onClick(placesList[holder.adapterPosition], holder.adapterPosition)
            placesList.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    fun setDeleteClickListener(listener: FavoriteItemClickListener) {
        delClickListener = listener
    }

    fun setPlaceClickListener(listener: FavoriteItemClickListener) {
        favClickListener = listener
    }

    fun setData(it: List<Place>?) {
        Log.d(TAG, "setData() called")
        if (it != null) {
            placesList = ArrayList(it)
            Log.d(TAG, "places are $placesList")
            notifyDataSetChanged()
        }
    }

    inner class FavoriteViewHolder(val binding: FavListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}