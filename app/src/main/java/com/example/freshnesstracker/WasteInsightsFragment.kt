package com.example.freshnesstracker

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment

class WasteInsightsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_waste_insights, container, false)

        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 82)
        progressAnimator.duration = 1500
        progressAnimator.start()

        return view
    }
}