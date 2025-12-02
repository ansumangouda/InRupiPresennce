package com.inrupipresennce.uiScreen.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.api.model.AttendanceResponse
import com.inrupipresennce.data.api.model.AttendanceTodayResponse
import com.inrupipresennce.data.api.model.LunchResponse
import com.inrupipresennce.data.repositry.AttendanceRepository
import com.inrupipresennce.utils.PreferenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

open class AttendanceViewModel(val repository: AttendanceRepository, val context: Context) : ViewModel() {
    private val _attendanceState = MutableStateFlow<AttendanceResponse?>(null)
    val attendanceState: StateFlow<AttendanceResponse?> = _attendanceState

    private val _todayAttendance = MutableStateFlow<AttendanceTodayResponse?>(null)
    val todayAttendance: StateFlow<AttendanceTodayResponse?> = _todayAttendance

    private val _lunchState = MutableStateFlow<LunchResponse?>(null)
    val lunchState: StateFlow<LunchResponse?> = _lunchState





    val adminId: Int
        get() = PreferenceHelper.getAdminId(context)

    fun uploadAttendance(adminId: Int,faceDescriptor: String, imageFile: File?, lat: Double?, lon: Double?) {
        viewModelScope.launch {
            val result = repository.uploadAttendance(adminId,faceDescriptor, imageFile, lat, lon)
            _attendanceState.value = result
        }
    }

    fun loadTodayAttendance() {
        viewModelScope.launch {
            val result = repository.getTodayAttendance(adminId)
            _todayAttendance.value = result
        }
    }
    fun clearAttendanceEvent() {
        _attendanceState.value = null
    }

    fun takeLunchBreak() {
        viewModelScope.launch {
            val response = repository.lunchBreak(adminId)
            _lunchState.value = response
        }
    }
    fun clearLunchEvent() {
        _lunchState.value = null
    }

}
