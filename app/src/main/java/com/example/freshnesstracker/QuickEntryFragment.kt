package com.example.freshnesstracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
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
                foodViewModel.addItem(itemName, if (diffInDays < 0) 0 else diffInDays)
                
                // Show notification when item is added
                val notificationHelper = NotificationHelper(requireContext())
                notificationHelper.showExpiringItemsNotification(1) 
                
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}