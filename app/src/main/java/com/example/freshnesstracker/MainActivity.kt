package com.example.freshnesstracker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val foodViewModel: FoodViewModel by viewModels()
    private val alertedItems = mutableSetOf<Int>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.POST_NOTIFICATIONS] == true) {
            showInitialNotification()
        }
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            startLocationService()
        }
    }

    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkAndSendAutomaticSms()
        } else {
            Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find the toolbar and set it as the action bar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.top_toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        // Navigation UI setup
        bottomNavigationView.setupWithNavController(navController)

        val fab = findViewById<FloatingActionButton>(R.id.fab_add)
        fab.setOnClickListener { view ->
            showFabPopupMenu(view)
        }

        // Initialize Notifications
        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        checkAndRequestPermissions()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Exit Application")
                    .setMessage("Are you sure you want to close Pantry Tracker?")
                    .setPositiveButton("Yes") { _, _ ->
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        })

        // Automatic SMS Alert Observer
        foodViewModel.foodItems.observe(this) { items ->
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                checkAndSendAutomaticSms(items)
            }
        }
    }

    private fun checkAndSendAutomaticSms(items: List<FoodItem>? = null) {
        val currentItems = items ?: foodViewModel.foodItems.value ?: return
        for (item in currentItems) {
            if (item.id !in alertedItems) {
                val days = extractDays(item.expiryInfo)
                if (days in 1..2) {
                    sendSmsAlert(item.name, days)
                    alertedItems.add(item.id)
                }
            }
        }
    }

    private fun extractDays(expiryInfo: String): Int {
        return try {
            if (expiryInfo.contains("days")) {
                expiryInfo.split(" ")[2].toInt()
            } else if (expiryInfo.contains("day")) {
                1
            } else {
                -1
            }
        } catch (e: Exception) {
            -1
        }
    }

    private fun sendSmsAlert(itemName: String, daysLeft: Int) {
        try {
            val phoneNumber = "+919876543210"
            val message = "Alert! $itemName will expire in $daysLeft days. Please use it soon."
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(this, "Automatic SMS Sent for $itemName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Silently fail for automatic alerts to not disturb user if permission issues occur
        }
    }

    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun showFabPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.fab_popup_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_manually -> {
                    val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    navHostFragment.navController.navigate(R.id.navigation_quick_entry)
                    true
                }
                R.id.action_scan_barcode -> {
                    Toast.makeText(this, "Scan feature coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            showInitialNotification()
            startLocationService()
        }
    }

    private fun showInitialNotification() {
        val sharedPreferences = getSharedPreferences("FreshnessTrackerPrefs", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("isFirstLaunch", true)) {
            val notificationHelper = NotificationHelper(this)
            notificationHelper.showExpiringItemsNotification(3)
            sharedPreferences.edit {
                putBoolean("isFirstLaunch", false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            R.id.action_notifications -> {
                Toast.makeText(this, "Notification History", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About Freshness Tracker")
            .setMessage("This app helps you track the freshness of your food items and reduce waste.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the service when the app is destroyed (optional)
        stopService(Intent(this, LocationService::class.java))
        stopService(Intent(this, ExpiryService::class.java))
    }
}