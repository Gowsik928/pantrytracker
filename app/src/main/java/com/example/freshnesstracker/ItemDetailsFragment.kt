package com.example.freshnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.material.button.MaterialButton

class ItemDetailsFragment : Fragment() {

    private val foodViewModel: FoodViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_details, container, false)
        val foodImage = view.findViewById<ImageView>(R.id.item_image)
        val itemTitle = view.findViewById<TextView>(R.id.item_title)
        val countdownTimer = view.findViewById<TextView>(R.id.countdown_timer)
        val usedItButton = view.findViewById<MaterialButton>(R.id.used_it_button)

        val foodItemId = arguments?.getInt("foodItemId") ?: 0
        foodImage.transitionName = "food_image_$foodItemId"

        foodViewModel.foodItems.observe(viewLifecycleOwner) { items ->
            val foodItem = items.find { it.id == foodItemId }
            if (foodItem != null) {
                itemTitle.text = foodItem.name
                countdownTimer.text = foodItem.expiryInfo
                // In a real app, you would load the image from the URL using a library like Glide or Picasso
                // foodImage.setImage...()

                usedItButton.setOnClickListener {
                    foodViewModel.useItem(foodItem)
                    findNavController().popBackStack()
                }
            }
        }

        return view
    }
}