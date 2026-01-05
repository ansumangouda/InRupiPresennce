
package com.inrupipresennce.uiScreen.ViewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inrupipresennce.data.repositry.LeaveRepository
import com.inrupipresennce.uiScreen.viewmodel.LeaveViewModel

class LeaveViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaveViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LeaveViewModel(LeaveRepository(context.applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
