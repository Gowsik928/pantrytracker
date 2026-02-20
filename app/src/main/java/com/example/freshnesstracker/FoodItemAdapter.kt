package com.example.freshnesstracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class FoodItemAdapter(
    private val onItemClick: (FoodItem, View) -> Unit
) : ListAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder>(FoodItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food, parent, false)
        return FoodItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        val foodItem = getItem(position)
        holder.bind(foodItem, onItemClick)
    }

    class FoodItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val statusBorder: View = itemView.findViewById(R.id.status_border)
        private val foodImage: ImageView = itemView.findViewById(R.id.food_image)
        private val foodName: TextView = itemView.findViewById(R.id.food_name)
        private val expiryInfo: TextView = itemView.findViewById(R.id.expiry_info)

        fun bind(foodItem: FoodItem, onItemClick: (FoodItem, View) -> Unit) {
            foodName.text = foodItem.name
            expiryInfo.text = foodItem.expiryInfo
            statusBorder.setBackgroundColor(foodItem.statusColor)
            foodImage.transitionName = "food_image_${foodItem.id}"
            // In a real app, you would load the image from the URL using a library like Glide or Picasso
            // foodImage.setImage...()

            itemView.setOnClickListener {
                onItemClick(foodItem, foodImage)
            }
        }
    }
}

class FoodItemDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
    override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
        return oldItem == newItem
    }
}