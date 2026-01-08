package com.inrupipresennce.ui.presence

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.model.PresenceRecord
import com.inrupipresennce.data.repositry.PresenceRepository
import com.inrupipresennce.data.valu.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class PresenceCalendarViewModel(context: Context, private val repository: PresenceRepository) : ViewModel() {

    private val _presenceRecords = MutableStateFlow<List<PresenceRecord>>(emptyList())
    val presenceRecords: StateFlow<List<PresenceRecord>> get() = _presenceRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    init {
        loadPresenceHistory()
    }

    data class DayDetail(
        val date: String,
        val punchIn: String?,
        val punchOut: String?
    )

    private val _selectedDayDetail = MutableStateFlow<DayDetail?>(null)
    val selectedDayDetail = _selectedDayDetail.asStateFlow()

    fun showDayDetail(detail: DayDetail) {
        _selectedDayDetail.value = detail
    }

    fun dismissDayDetail() {
        _selectedDayDetail.value = null
    }

    fun getDayDetail(
        date: String,
        records: List<PresenceRecord>
    ): DayDetail {

        val dayRecords = records.filter {
            it.punch_in_at.startsWith(date)
        }

        val punchIn = dayRecords.minByOrNull { it.punch_in_at }?.punch_in_at
        val punchOut = dayRecords
            .mapNotNull { it.punch_out_at }
            .maxOrNull()

        return DayDetail(
            date = date,
            punchIn = punchIn,
            punchOut = punchOut
        )
    }


    fun loadPresenceHistory() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val year = _currentYearMonth.value.year
                val month = _currentYearMonth.value.monthValue

                val rawRecords = repository.getPresenceHistory(year, month)

                val fixedRecords = rawRecords.map { record ->
                    record.copy(
                        punch_in_image = fixImageUrl(record.punch_in_image)
                    )
                }

                _presenceRecords.value = fixedRecords
                if (_selectedDate.value == null) {
                    _selectedDate.value = LocalDate.now().toString()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun goToPreviousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
        loadPresenceHistory()
    }

    fun goToNextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
        loadPresenceHistory()
    }

    private fun fixImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null

        val baseUrl = Constants.BASE_URL

        return when {
            imagePath.startsWith("http") -> imagePath
            imagePath.startsWith("/storage/") -> baseUrl + imagePath.removePrefix("/")
            imagePath.startsWith("storage/") -> baseUrl + imagePath
            imagePath.startsWith("attendance_images/") -> baseUrl + "storage/" + imagePath
            imagePath.startsWith("/attendance_images/") -> baseUrl + "storage" + imagePath
            else -> baseUrl + "storage/" + imagePath
        }
    }

    data class AttendanceSummary(
        val totalDays: Int,
        val presentDays: Int,
        val absentDays: Int,
        val onTimeDays: Int,
        val lateDays: Int,
        val remainingDays: Int
    )

    fun calculateAttendanceSummary(records: List<PresenceRecord>): AttendanceSummary {
        val yearMonth = _currentYearMonth.value
        val today = LocalDate.now()

        val allDaysInMonth = (1..yearMonth.lengthOfMonth())
            .map { yearMonth.atDay(it) }
            .filter { it.dayOfWeek.value != 7 } // Exclude Sunday

        val totalWorkingDays = allDaysInMonth.size

        val presentDays = records.map { LocalDate.parse(it.punch_in_at.substring(0, 10)) }.distinct()

        var onTimeDays = 0
        var lateDays = 0
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        records.groupBy { it.punch_in_at.substring(0, 10) }.values.forEach { dayRecords ->
            val firstPunchIn = dayRecords.minByOrNull { it.punch_in_at }
            firstPunchIn?.let {
                try {
                    val punchInTime = LocalDateTime.parse(it.punch_in_at, formatter)
                    if (punchInTime.hour < 10 || (punchInTime.hour == 10 && punchInTime.minute <= 0)) {
                        onTimeDays++
                    } else {
                        lateDays++
                    }
                } catch (e: Exception) {
                    // Ignore parsing errors on invalid dates
                }
            }
        }

        val (absentDays, remainingDays) = when {
            yearMonth.isBefore(YearMonth.from(today)) -> {
                Pair(totalWorkingDays - presentDays.size, 0)
            }
            yearMonth.isAfter(YearMonth.from(today)) -> {
                Pair(0, totalWorkingDays)
            }
            else -> { // Current month
                val pastOrTodayWorkingDays = allDaysInMonth.filter { !it.isAfter(today) }
                val futureWorkingDays = allDaysInMonth.count { it.isAfter(today) }
                val absent = pastOrTodayWorkingDays.count { it !in presentDays }
                Pair(absent, futureWorkingDays)
            }
        }

        return AttendanceSummary(
            totalDays = totalWorkingDays,
            presentDays = presentDays.size,
            absentDays = absentDays,
            onTimeDays = onTimeDays,
            lateDays = lateDays,
            remainingDays = remainingDays
        )
    }

    data class DailyWorkReport(
        val date: String,
        val hoursWorked: Float,
        val hasPunchOut: Boolean
    )

    fun getDailyWorkReport(records: List<PresenceRecord>): List<DailyWorkReport> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val grouped = records.groupBy { it.punch_in_at.substring(0, 10) }

        return grouped.map { (dateStr, dayRecords) ->
            val punchIn = dayRecords.minByOrNull { it.punch_in_at }?.punch_in_at
            val punchOut = dayRecords.maxByOrNull { it.punch_out_at ?: it.punch_in_at }?.punch_out_at

            val hasPunchOut = !punchOut.isNullOrBlank()

            val hours = if (hasPunchOut && !punchIn.isNullOrBlank()) {
                try {
                    val inTime = LocalDateTime.parse(punchIn, formatter)
                    val outTime = LocalDateTime.parse(punchOut, formatter)
                    val diff = java.time.Duration.between(inTime, outTime).toMinutes().toFloat() / 60f
                    diff.coerceIn(0f, 12f) // clamp hours to max 12 hrs
                } catch (e: Exception) {
                    0f
                }
            } else 0f

            DailyWorkReport(
                date = dateStr,
                hoursWorked = hours,
                hasPunchOut = hasPunchOut
            )
        }.sortedBy { it.date }
    }
}
