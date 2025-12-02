package com.inrupipresennce.ui.presence

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.api.model.PresenceRecord
import com.inrupipresennce.data.repositry.PresenceRepository
import com.inrupipresennce.data.valu.Constants

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PresenceCalendarViewModel(context: Context, repository: PresenceRepository) : ViewModel() {

    private val repository = PresenceRepository(context)

    private val _presenceRecords = MutableStateFlow<List<PresenceRecord>>(emptyList())
    val presenceRecords: StateFlow<List<PresenceRecord>> get() = _presenceRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(false)


    val isLoading = _isLoading.asStateFlow()
    init {
        // ✅ Load only once when ViewModel is created
        loadPresenceHistory(context)
    }

    fun loadPresenceHistory(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // ✅ Call repository instead of API directly
                val rawRecords = repository.getPresenceHistory()

                // ✅ Fix image URLs before storing
                val fixedRecords = rawRecords.map { record ->
                    record.copy(
                        punch_in_image = fixImageUrl(record.punch_in_image)
                    )
                }

                _presenceRecords.value = fixedRecords

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ Cleans and fixes all possible URL formats
    private fun fixImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null

        val baseUrl = Constants.BASE_URL  // ✅ Use shared constant


        return when {
            imagePath.startsWith("http") -> imagePath // already full URL
            imagePath.startsWith("/storage/") -> baseUrl + imagePath.removePrefix("/")
            imagePath.startsWith("storage/") -> baseUrl + imagePath
            imagePath.startsWith("attendance_images/") -> baseUrl + "storage/" + imagePath
            imagePath.startsWith("/attendance_images/") -> baseUrl + "storage" + imagePath
            else -> baseUrl + "storage/" + imagePath // fallback
        }
    }



    data class AttendanceSummary(
        val totalDays: Int,
        val presentDays: Int,
        val absentDays: Int,
        val onTimeDays: Int,
        val lateDays: Int,
        val remainingDays: Int // 👈 NEW FIELD
    )

    fun calculateAttendanceSummary(records: List<PresenceRecord>): AttendanceSummary {
        if (records.isEmpty()) return AttendanceSummary(0, 0, 0, 0, 0, 0)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val now = LocalDateTime.now()
        val currentMonth = now.monthValue
        val currentYear = now.year
        val todayDate = now.toLocalDate()

        // ✅ Filter records only for the current month
        val currentMonthRecords = records.filter { record ->
            try {
                val date = LocalDateTime.parse(record.punch_in_at, formatter)
                date.monthValue == currentMonth && date.year == currentYear
            } catch (e: Exception) {
                false
            }
        }

        // ✅ Group by unique working days (one per date)
        val groupedByDate = currentMonthRecords.groupBy {
            it.punch_in_at.substring(0, 10)
        }

        val presentDays = groupedByDate.keys.map { LocalDate.parse(it) }

        // ✅ Count on-time and late days
        var onTimeDays = 0
        var lateDays = 0

        currentMonthRecords.forEach { record ->
            try {
                val punchIn = LocalDateTime.parse(record.punch_in_at, formatter)
                val hour = punchIn.hour
                val minute = punchIn.minute

                if (hour < 10 || (hour == 10 && minute <= 0)) {
                    onTimeDays++
                } else {
                    lateDays++
                }
            } catch (_: Exception) {}
        }

        // ✅ Generate all days in current month (excluding Sundays)
        val allDays = (1..java.time.YearMonth.of(currentYear, currentMonth).lengthOfMonth()).map {
            LocalDate.of(currentYear, currentMonth, it)
        }.filter { it.dayOfWeek.value != 7 } // Exclude Sunday

        // ✅ Working days before or equal to today (for absence calculation)
        val workingDaysUntilToday = allDays.filter { !it.isAfter(todayDate) }

        // ✅ Days left after today (future working days)
        val remainingWorkingDays = allDays.count { it.isAfter(todayDate) }

        // ✅ Count absents (past working days not in present list)
        val absentDays = workingDaysUntilToday.count { it !in presentDays }

        // ✅ Total working days (excluding Sundays)
        val totalWorkingDays = allDays.size

        return AttendanceSummary(
            totalDays = totalWorkingDays,
            presentDays = presentDays.size,
            absentDays = absentDays,
            onTimeDays = onTimeDays,
            lateDays = lateDays,
            remainingDays = remainingWorkingDays
        )
    }




    data class DailyWorkReport(
        val date: String,
        val hoursWorked: Float,
        val hasPunchOut: Boolean
    )

    fun getDailyWorkReport(records: List<PresenceRecord>): List<DailyWorkReport> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        // Group by day (YYYY-MM-DD)
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
