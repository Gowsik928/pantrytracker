package com.example.freshnesstracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import java.util.*

class FreshnessDashboardFragment : Fragment() {

    private val foodViewModel: FoodViewModel by activityViewModels()
    private lateinit var foodItemAdapter: FoodItemAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            findNearbyShops()
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_freshness_dashboard, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

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

        val btnStartService = view.findViewById<MaterialButton>(R.id.btnStartService)
        val btnStopService = view.findViewById<MaterialButton>(R.id.btnStopService)
        val btnNearbyShops = view.findViewById<MaterialButton>(R.id.btnNearbyShops)

        btnStartService.setOnClickListener {
            val serviceIntent = Intent(requireContext(), ExpiryService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requireActivity().startForegroundService(serviceIntent)
            } else {
                requireActivity().startService(serviceIntent)
            }
        }

        btnStopService.setOnClickListener {
            requireActivity().stopService(Intent(requireContext(), ExpiryService::class.java))
        }

        btnNearbyShops.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                findNearbyShops()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        return view
    }

    private fun findNearbyShops() {
        try {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lon, 1)
                    val cityName = addresses?.firstOrNull()?.locality ?: "Current Location"
                    
                    Toast.makeText(requireContext(), "Searching grocery shops near $cityName", Toast.LENGTH_SHORT).show()
                    
                    val gmmIntentUri = Uri.parse("geo:$lat,$lon?q=grocery+store")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    
                    if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(mapIntent)
                    } else {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/grocery+store/@$lat,$lon,15z"))
                        startActivity(browserIntent)
                    }
                } else {
                    Toast.makeText(requireContext(), "Unable to fetch location. Please ensure GPS is enabled.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}