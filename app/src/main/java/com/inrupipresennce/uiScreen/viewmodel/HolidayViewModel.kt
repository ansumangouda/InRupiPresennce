package com.inrupipresennce.uiScreen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.model.Holiday
import com.inrupipresennce.data.repositry.HolidayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HolidayViewModel(private val repository: HolidayRepository) : ViewModel() {

    private val _holidays = MutableStateFlow<Map<String, List<Holiday>>>(emptyMap())
    val holidays: StateFlow<Map<String, List<Holiday>>> = _holidays

    fun getHolidays() {
        viewModelScope.launch {
            try {
                val holidayData = repository.getHolidays()
                _holidays.value = holidayData
                Log.d("HolidayViewModel", "Holidays loaded: $holidayData")
            } catch (e: Exception) {
                Log.e("HolidayViewModel", "Error fetching holidays", e)
            }
        }
    }
}