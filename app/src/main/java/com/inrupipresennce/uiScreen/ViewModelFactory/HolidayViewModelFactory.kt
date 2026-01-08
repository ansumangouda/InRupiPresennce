package com.inrupipresennce.uiScreen.ViewModelFactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inrupipresennce.data.repositry.HolidayRepository
import com.inrupipresennce.uiScreen.viewmodel.HolidayViewModel

class HolidayViewModelFactory(private val repository: HolidayRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HolidayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HolidayViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}