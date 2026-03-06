package com.example.freshnesstracker

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class FoodItem(
    val id: Int, 
    val name: String, 
    val expiryInfo: String, 
    val statusColor: Int, 
    val imageUrl: String,
    val isUsed: Boolean = false
)

class FoodViewModel : ViewModel() {

    private val _foodItems = MutableLiveData<List<FoodItem>>().apply {
        value = listOf(
            FoodItem(1, "Organic Baby Spinach", "Expires in 3 days", Color.GREEN, ""),
            FoodItem(2, "Organic Chicken Breast", "Expires in 1 day", Color.YELLOW, ""),
            FoodItem(3, "Whole Milk", "Expired", Color.RED, ""),
            FoodItem(4, "Avocado", "Expires in 2 days", Color.GREEN, ""),
            FoodItem(5, "Salad Mix", "Expires in 1 day", Color.YELLOW, ""),
            FoodItem(6, "Ground Beef", "Expired", Color.RED, "")
        )
    }
    val foodItems: LiveData<List<FoodItem>> = _foodItems

    fun addItem(name: String, expiryInDays: Int) {
        val currentList = _foodItems.value.orEmpty().toMutableList()
        val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
        val expiryInfo = if (expiryInDays > 0) "Expires in $expiryInDays days" else "Expired"
        val statusColor = when {
            expiryInDays > 3 -> Color.GREEN
            expiryInDays > 0 -> Color.YELLOW
            else -> Color.RED
        }
        currentList.add(FoodItem(newId, name, expiryInfo, statusColor, "", false))
        _foodItems.value = currentList
    }

    fun useItem(item: FoodItem) {
        val currentList = _foodItems.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isUsed = true)
            _foodItems.value = currentList
        }
    }

    fun deleteItem(item: FoodItem) {
        val currentList = _foodItems.value.orEmpty().toMutableList()
        currentList.removeIf { it.id == item.id }
        _foodItems.value = currentList
    }
}