package com.inrupipresennce.uiScreen.ViewModelFactory


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inrupipresennce.data.repositry.PresenceRepository
import com.inrupipresennce.ui.presence.PresenceCalendarViewModel

class PresenceCalendarViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PresenceCalendarViewModel::class.java)) {
            val repository = PresenceRepository(context)
            return PresenceCalendarViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}