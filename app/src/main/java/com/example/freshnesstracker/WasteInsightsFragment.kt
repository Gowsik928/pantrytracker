package com.example.freshnesstracker

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.util.*

class WasteInsightsFragment : Fragment() {

    private val foodViewModel: FoodViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_waste_insights, container, false)

        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val efficiencyText = view.findViewById<TextView>(R.id.efficiency_percentage)
        val statusMessage = view.findViewById<TextView>(R.id.efficiency_score_message)
        val savedValueText = view.findViewById<TextView>(R.id.total_saved_value)
        val wastedValueText = view.findViewById<TextView>(R.id.total_wasted_value)

        foodViewModel.foodItems.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                val totalItems = items.size
                val freshItems = items.count { it.statusColor == Color.GREEN }
                val wastedItems = items.count { it.statusColor == Color.RED }
                
                // Calculate Efficiency Score
                val score = (freshItems.toFloat() / totalItems * 100).toInt()

                // Calculate Financial Impact (Mock value: $5.00 per item)
                val estimatedSaved = freshItems * 5.0
                val estimatedWasted = wastedItems * 5.0

                // Animate progress bar
                val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, score)
                progressAnimator.duration = 1000
                progressAnimator.start()

                // Update UI Text
                efficiencyText.text = "$score%"
                savedValueText.text = String.format(Locale.getDefault(), "$%.2f", estimatedSaved)
                wastedValueText.text = String.format(Locale.getDefault(), "$%.2f", estimatedWasted)
                
                statusMessage.text = when {
                    score >= 80 -> "Great job! You're saving more food."
                    score >= 50 -> "Good effort, but watch those expiry dates."
                    else -> "You have several items about to expire. Use them soon!"
                }
            } else {
                progressBar.progress = 0
                efficiencyText.text = "0%"
                savedValueText.text = "$0.00"
                wastedValueText.text = "$0.00"
                statusMessage.text = "Add some items to track your efficiency."
            }
        }

        return view
    }
}