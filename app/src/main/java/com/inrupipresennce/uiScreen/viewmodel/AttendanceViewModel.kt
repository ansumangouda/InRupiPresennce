package com.inrupipresennce.uiScreen.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.api.model.AttendanceResponse
import com.inrupipresennce.data.api.model.AttendanceTodayResponse
import com.inrupipresennce.data.api.model.BirthdayResponse
import com.inrupipresennce.data.api.model.EarlyTeammate
import com.inrupipresennce.data.api.model.LunchResponse
import com.inrupipresennce.data.api.model.Teammate
import com.inrupipresennce.data.repositry.AttendanceRepository
import com.inrupipresennce.utils.PreferenceHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

open class AttendanceViewModel(val repository: AttendanceRepository, val context: Context) : ViewModel() {
    private val _attendanceState = MutableStateFlow<AttendanceResponse?>(null)
    val attendanceState: StateFlow<AttendanceResponse?> = _attendanceState

    private val _todayAttendance = MutableStateFlow<AttendanceTodayResponse?>(null)
    val todayAttendance: StateFlow<AttendanceTodayResponse?> = _todayAttendance

    private val _lunchState = MutableStateFlow<LunchResponse?>(null)
    val lunchState: StateFlow<LunchResponse?> = _lunchState

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _birthdayTeammates = MutableStateFlow<List<Teammate>>(emptyList())
    val birthdayTeammates: StateFlow<List<Teammate>> = _birthdayTeammates

    private val _seeMoreCount = MutableStateFlow<String?>(null)
    val seeMoreCount: StateFlow<String?> = _seeMoreCount


    private val _offTodayTeammates = MutableStateFlow<List<Teammate>>(emptyList())
    val offTodayTeammates: StateFlow<List<Teammate>> = _offTodayTeammates


    private val _earlyBirdToday = MutableStateFlow<List<EarlyTeammate>>(emptyList())
    val earlyBirdToday: StateFlow<List<EarlyTeammate>> = _earlyBirdToday

    // MONTHLY RANKING
    private val _earlyBirdMonthly = MutableStateFlow<List<EarlyTeammate>>(emptyList())
    val earlyBirdMonthly: StateFlow<List<EarlyTeammate>> = _earlyBirdMonthly



    val adminId: Int
        get() = PreferenceHelper.getAdminId(context)

    fun uploadAttendance(adminId: Int,faceDescriptor: String, imageFile: File?, lat: Double?, lon: Double?) {
        viewModelScope.launch {
            val result = repository.uploadAttendance(adminId,faceDescriptor, imageFile, lat, lon)
            if (result?.success == true) {
                _successMessage.value = result.message
            } else {
                _errorMessage.value = result?.message ?: "An unknown error occurred"
            }
            _attendanceState.value = result
        }
    }

    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }

    fun loadTodayAttendance() {
        viewModelScope.launch {
            val result = repository.getTodayAttendance(adminId)
            _todayAttendance.value = result
        }
    }

    fun loadBirthdays() {
        viewModelScope.launch {
            val response = repository.getBirthdays()

            if (response?.status == true) {

                val todayList = response.today.orEmpty().map {
                    Teammate(
                        name = it.name,
                        role = "Today 🎉",
                        imageUrl = it.image,
                        birthdayText = "Today 🎉"

                    )
                }

                val upcomingList = response.upcoming.orEmpty().map {
                    Teammate(
                        name = it.name,
                        role = "Upcoming",
                        imageUrl = it.image,
                        birthdayText = formatBirthday(it.dob)

                    )
                }

                val finalList = todayList + upcomingList

                _birthdayTeammates.value = finalList

                val extra =
                    (response.count?.today ?: 0) + (response.count?.upcoming ?: 0) - finalList.size

                _seeMoreCount.value = if (extra > 0) "+$extra" else null
            }
        }
    }
    fun loadOffToday() {
        viewModelScope.launch {
            val response = repository.getOffToday()

            if (response?.status == true) {
                _offTodayTeammates.value = response.data.map {
                    Teammate(
                        name = it.name,
                        role = "OFF ❌",
                        imageUrl = it.image,
                        birthdayText = "OFF ❌"

                    )
                }
            }
        }
    }

    fun loadEarlyBirds() {
        viewModelScope.launch {
            val response = repository.getEarlyBirdReport()

            if (response?.status == true) {

                _earlyBirdToday.value = response.today_early_birds.map {
                    EarlyTeammate(
                        name = it.name,
                        role = "Early Bird 🏆",
                        subText = it.punch_in_time,
                        imageUrl = it.image            // 👈 ADD THIS
                    )


                }

                _earlyBirdMonthly.value = response.monthly_ranking.map {
                    EarlyTeammate(
                            name = it.name,
                    role = "Rank #${it.rank}",
                    subText = "Early days: ${it.early_days_count}",
                    imageUrl = it.image            // 👈 ADD THIS
                    )
                }
            }
        }
    }


    fun clearAttendanceEvent() {
        _attendanceState.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
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

fun formatBirthday(dob: String): String {
    return try {
        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

        val date = apiFormat.parse(dob) ?: return ""

        displayFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}



