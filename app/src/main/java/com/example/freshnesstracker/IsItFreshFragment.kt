package com.example.freshnesstracker

import android.graphics.Color
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
import com.google.android.material.tabs.TabLayout

class IsItFreshFragment : Fragment() {

    private val foodViewModel: FoodViewModel by activityViewModels()
    private lateinit var foodItemAdapter: FoodItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_is_it_fresh, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.food_item_list)
        val tabs = view.findViewById<TabLayout>(R.id.tabs)
        
        recyclerView.layoutManager = LinearLayoutManager(context)

        foodItemAdapter = FoodItemAdapter { foodItem, imageView ->
            val extras = FragmentNavigatorExtras(imageView to "food_image_${foodItem.id}")
            val bundle = bundleOf("foodItemId" to foodItem.id)
            findNavController().navigate(
                R.id.action_isItFreshFragment_to_itemDetailsFragment,
                bundle,
                null,
                extras
            )
        }
        recyclerView.adapter = foodItemAdapter

        // Observe changes and filter based on current tab
        foodViewModel.foodItems.observe(viewLifecycleOwner) { items ->
            filterItems(items, tabs.selectedTabPosition)
        }

        // Add tab listener to re-filter when tab changes
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                foodViewModel.foodItems.value?.let { items ->
                    filterItems(items, tab?.position ?: 0)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }

    private fun filterItems(items: List<FoodItem>, position: Int) {
        val filteredList = when (position) {
            1 -> items.filter { it.statusColor == Color.YELLOW } // Expiring
            2 -> items.filter { it.statusColor == Color.RED }    // Expired
            else -> items // All Items
        }
        foodItemAdapter.submitList(filteredList)
    }
}