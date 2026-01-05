package com.inrupipresennce.uiScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.model.PayslipData
import com.inrupipresennce.data.repositry.PayslipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PayslipViewModel(
    private val repository: PayslipRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _payslips = MutableStateFlow<List<PayslipData>>(emptyList())
    val payslips: StateFlow<List<PayslipData>> = _payslips

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count

    fun loadPayslips() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getPayslips()

                if (response.status && !response.data.isNullOrEmpty()) {
                    _payslips.value = response.data
                    _count.value = response.data.size
                } else {
                    _payslips.value = emptyList()
                    _count.value = 0
                    _message.value = response.message
                }

            } catch (e: Exception) {
                _message.value = "Something went wrong"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
