package com.inrupipresennce.data.repositry

import android.content.Context
import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.model.PresenceRecord
import com.inrupipresennce.utils.PreferenceHelper
import com.inrupipresennce.data.model.AttendanceRequest

class PresenceRepository(private val context: Context) {

    suspend fun getPresenceHistory(year: Int, month: Int): List<PresenceRecord> {
        val adminId = PreferenceHelper.getAdminId(context)
        val request = AttendanceRequest(adminId = adminId, year = year, month = month)
        val response = ApiClient.api.getPresenceHistory(request)
        return if (response.success) response.attendance_records else emptyList()
    }
}