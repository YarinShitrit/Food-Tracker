package com.example.burgertracker.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.burgertracker.databinding.ReviewListItemBinding
import com.example.burgertracker.placesData.PlaceReview

class ReviewsListAdapter : RecyclerView.Adapter<ReviewsListAdapter.ReviewViewHolder>() {
    private var reviewsList: ArrayList<PlaceReview>? = null
    private lateinit var reviewBinding: ReviewListItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        reviewBinding =
            ReviewListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(reviewBinding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.reviewBinding.name.text = "Name: " + reviewsList!![position].author_name
        holder.reviewBinding.rating.text = "Rating: " + reviewsList!![position].rating.toString()
        holder.reviewBinding.timeDescription.text =
            reviewsList!![position].relative_time_description
        holder.reviewBinding.textReview.text = reviewsList!![position].text
    }

    override fun getItemCount(): Int {
        return if (reviewsList != null)
            reviewsList!!.size
        else
            0
    }

    fun setData(reviews: ArrayList<PlaceReview>) {
        reviewsList = reviews
        notifyDataSetChanged()
    }

    class ReviewViewHolder(val reviewBinding: ReviewListItemBinding) :
        RecyclerView.ViewHolder(reviewBinding.root)
}