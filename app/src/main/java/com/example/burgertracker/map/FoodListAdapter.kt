package com.example.burgertracker.map

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.burgertracker.R
import kotlinx.android.synthetic.main.food_list_item.view.*


interface ViewHolderClickListener {
    fun click()

}

class FoodListAdapter :
    RecyclerView.Adapter<FoodListAdapter.FoodItemViewHolder>() {
    lateinit var itemClicked: String
    private lateinit var listener: ViewHolderClickListener
    private lateinit var foodList: ArrayList<String>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        return FoodItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.food_list_item, null)
        )
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.itemView.food_item_text.text = foodList[position]
        holder.itemView.setOnClickListener {
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


