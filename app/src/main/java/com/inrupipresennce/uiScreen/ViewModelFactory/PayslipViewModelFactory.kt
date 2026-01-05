package com.inrupipresennce.uiScreen.ViewModelFactory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inrupipresennce.data.repositry.PayslipRepository
import com.inrupipresennce.uiScreen.viewmodel.PayslipViewModel

class PayslipViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PayslipViewModel(PayslipRepository(context)) as T
    }
}
