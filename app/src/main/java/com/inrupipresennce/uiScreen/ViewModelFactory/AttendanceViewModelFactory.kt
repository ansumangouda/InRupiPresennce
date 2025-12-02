package com.inrupipresennce.uiScreen.ViewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inrupipresennce.data.repositry.AttendanceRepository
import com.inrupipresennce.uiScreen.viewmodel.AttendanceViewModel


class AttendanceViewModelFactory(private val repository: AttendanceRepository, private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            return AttendanceViewModel(repository,context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}