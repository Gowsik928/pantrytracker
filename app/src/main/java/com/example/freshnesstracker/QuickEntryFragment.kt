package com.example.freshnesstracker

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class QuickEntryFragment : Fragment() {

    private val foodViewModel: FoodViewModel by activityViewModels()
    private var selectedCalendar = Calendar.getInstance()
    private var reminderDate = ""
    private var reminderTime = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quick_entry, container, false)

        setupReminderFeature(view)

        val itemNameEditText = view.findViewById<TextInputEditText>(R.id.item_name_edit_text)
        val addToInventoryButton = view.findViewById<MaterialButton>(R.id.add_to_inventory_button)
        val quantityTextView = view.findViewById<TextView>(R.id.quantity_text)
        val incrementButton = view.findViewById<MaterialButton>(R.id.increment_button)
        val decrementButton = view.findViewById<MaterialButton>(R.id.decrement_button)
        val expiryDatePicker = view.findViewById<TextView>(R.id.expiry_date_picker)
        
        val chip3Days = view.findViewById<Chip>(R.id.chip_3_days)
        val chip1Week = view.findViewById<Chip>(R.id.chip_1_week)
        val chip2Weeks = view.findViewById<Chip>(R.id.chip_2_weeks)

        val popupClickListener = View.OnClickListener { v ->
            val popup = PopupMenu(requireContext(), v)
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_set_reminder -> {
                        Toast.makeText(requireContext(), "Reminder Set Successfully", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_clear_date -> {
                        expiryDatePicker.text = "Select Expiry Date"
                        Toast.makeText(requireContext(), "Date Cleared", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_custom_days -> {
                        Toast.makeText(requireContext(), "Select Custom Days", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        chip3Days.setOnClickListener(popupClickListener)
        chip1Week.setOnClickListener(popupClickListener)
        chip2Weeks.setOnClickListener(popupClickListener)

        var quantity = 1

        // Date Picker Setup
        updateDateDisplay(expiryDatePicker)
        expiryDatePicker.setOnClickListener {
            showDatePicker(expiryDatePicker)
        }

        incrementButton.setOnClickListener {
            quantity++
            quantityTextView.text = quantity.toString()
        }

        decrementButton.setOnClickListener {
            if (quantity > 1) {
                quantity--
                quantityTextView.text = quantity.toString()
            }
        }

        addToInventoryButton.setOnClickListener {
            val itemName = itemNameEditText.text.toString()
            if (itemName.isNotBlank()) {
                showConfirmationDialog(itemName, quantity)
            } else {
                itemNameEditText.error = "Please enter an item name"
            }
        }

        return view
    }

    private fun setupReminderFeature(view: View) {
        val etName = view.findViewById<TextInputEditText>(R.id.et_reminder_item_name)
        val btnDate = view.findViewById<Button>(R.id.btn_reminder_date)
        val btnTime = view.findViewById<Button>(R.id.btn_reminder_time)
        val swStatus = view.findViewById<MaterialSwitch>(R.id.sw_reminder_status)
        val tvStatusLabel = view.findViewById<TextView>(R.id.tv_status_label)
        val btnSave = view.findViewById<Button>(R.id.btn_save_reminder)
        
        displaySavedReminder(view)

        swStatus.setOnCheckedChangeListener { _, isChecked ->
            tvStatusLabel.text = if (isChecked) "Status: Used" else "Status: Not Used"
        }

        btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                reminderDate = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year)
                btnDate.text = "Date: $reminderDate"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val hour12 = if (hour % 12 == 0) 12 else hour % 12
                reminderTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)
                btnTime.text = "Time: $reminderTime"
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            if (name.isNotEmpty() && reminderDate.isNotEmpty() && reminderTime.isNotEmpty()) {
                saveReminder(name, reminderDate, reminderTime, swStatus.isChecked)
                displaySavedReminder(view)
                Toast.makeText(requireContext(), "Reminder Saved Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill all reminder details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveReminder(name: String, date: String, time: String, isUsed: Boolean) {
        val prefs = requireContext().getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("item_name", name)
            putString("reminder_date", date)
            putString("reminder_time", time)
            putBoolean("is_used", isUsed)
        }
    }

    private fun displaySavedReminder(view: View) {
        val prefs = requireContext().getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
        val tvDisplay = view.findViewById<TextView>(R.id.tv_reminder_display)
        val name = prefs.getString("item_name", null)
        
        if (name != null) {
            val date = prefs.getString("reminder_date", "")
            val time = prefs.getString("reminder_time", "")
            val status = if (prefs.getBoolean("is_used", false)) "Used" else "Not Used"
            tvDisplay.text = "Item Name : $name\nReminder Date : $date\nReminder Time : $time\nStatus : $status"
        } else {
            tvDisplay.text = "No Reminder Saved"
        }
    }

    private fun showDatePicker(expiryDatePicker: TextView) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedCalendar.set(Calendar.YEAR, year)
                selectedCalendar.set(Calendar.MONTH, month)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay(expiryDatePicker)
            },
            selectedCalendar.get(Calendar.YEAR),
            selectedCalendar.get(Calendar.MONTH),
            selectedCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateDisplay(textView: TextView) {
        val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        textView.text = format.format(selectedCalendar.time)
    }

    private fun showConfirmationDialog(itemName: String, quantity: Int) {
        val diffInMs = selectedCalendar.timeInMillis - System.currentTimeMillis()
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMs).toInt() + 1
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add Item")
            .setMessage("Are you sure you want to add $quantity x $itemName to your inventory?")
            .setPositiveButton("Add") { _, _ ->
                val progressDialog = ProgressDialog(requireContext())
                progressDialog.setTitle("Saving Item")
                progressDialog.setMessage("Please wait...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                    foodViewModel.addItem(itemName, if (diffInDays < 0) 0 else diffInDays)
                    
                    // Show notification when item is added
                    val notificationHelper = NotificationHelper(requireContext())
                    notificationHelper.showExpiringItemsNotification(1) 
                    
                    Toast.makeText(requireContext(), "Item Saved Successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }, 2000)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}