package com.example.freshnesstracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FreshnessDashboardFragment : Fragment() {

    private val foodViewModel: FoodViewModel by activityViewModels()
    private lateinit var foodItemAdapter: FoodItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_freshness_dashboard, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.inventory_list)
        recyclerView.layoutManager = LinearLayoutManager(context)

        foodItemAdapter = FoodItemAdapter { foodItem, imageView ->
            val extras = FragmentNavigatorExtras(imageView to "food_image_${foodItem.id}")
            val bundle = bundleOf("foodItemId" to foodItem.id)
            findNavController().navigate(
                R.id.action_freshnessDashboardFragment_to_itemDetailsFragment,
                bundle,
                null,
                extras
            )
        }
        recyclerView.adapter = foodItemAdapter

        foodViewModel.foodItems.observe(viewLifecycleOwner) {
            foodItemAdapter.submitList(it)
        }

        return view
    }
}