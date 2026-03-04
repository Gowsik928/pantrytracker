package com.example.freshnesstracker

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class QuickEntryFragment : Fragment() {

    private val foodViewModel: FoodViewModel by activityViewModels()
    private var selectedCalendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quick_entry, container, false)

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