package com.example.burgertracker.map

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.burgertracker.R
import com.example.burgertracker.databinding.FoodListItemBinding


interface ViewHolderClickListener {
    fun click()

}

class FoodListAdapter :
    RecyclerView.Adapter<FoodListAdapter.FoodItemViewHolder>() {
    private lateinit var foodItemBinding: FoodListItemBinding
    lateinit var itemClicked: String
    private lateinit var listener: ViewHolderClickListener
    private lateinit var foodList: ArrayList<String>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        foodItemBinding = FoodListItemBinding.inflate(LayoutInflater.from(parent.context))
        return FoodItemViewHolder(
            foodItemBinding.root

        )
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        foodItemBinding.foodItemBtn.text = foodList[position]
        holder.itemView.findViewById<Button>(R.id.food_item_btn).setOnClickListener {
            itemClicked = foodList[position]
            listener.click()
            Log.d("FoodViewHolder", foodList[position])
        }
    }

    override fun getItemCount(): Int {
        return foodList.size

    }

    fun setClickListener(viewHolderClickListener: ViewHolderClickListener) {
        listener = viewHolderClickListener
    }

    fun setData(data: ArrayList<String>) {
        foodList = data
        notifyDataSetChanged()
    }

    class FoodItemViewHolder(view: View) :
        RecyclerView.ViewHolder(view)

}


