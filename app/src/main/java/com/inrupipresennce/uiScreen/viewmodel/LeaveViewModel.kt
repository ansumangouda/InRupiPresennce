package com.inrupipresennce.uiScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.model.LeaveItem
import com.inrupipresennce.data.model.LeaveSummary
import com.inrupipresennce.data.repositry.LeaveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LeaveViewModel(
    private val repository: LeaveRepository
) : ViewModel() {

    val isLoading = MutableStateFlow(false)
    val leaveSummary = MutableStateFlow<LeaveSummary?>(null)
    val leaves = MutableStateFlow<List<LeaveItem>>(emptyList())
    val message = MutableStateFlow<String?>(null)

    fun loadLeaves(year: Int? = null) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val res = repository.fetchLeaves(year)
                if (res.success) {
                    leaveSummary.value = res.leave_summary
                    leaves.value = res.leaves ?: emptyList()
                } else {
                    message.value = res.message
                }
            } finally {
                isLoading.value = false
            }
        }
    }

    fun applyLeave(
        from: String,
        to: String,
        type: String,
        reason: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val res = repository.applyLeave(from, to, type, reason)
                if (res.success) {
                    onSuccess()
                } else {
                    message.value = res.message
                }
            } catch (e: HttpException) {
                if (e.code() == 422) {
                    message.value = "Leave already exists for these dates"
                } else {
                    message.value = "An error occurred: ${e.message()}"
                }
            } finally {
                isLoading.value = false
            }
        }
    }
}
