package com.inrupipresennce.data.repositry

import android.content.Context
import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.api.model.PresenceRecord
import com.inrupipresennce.utils.PreferenceHelper


class PresenceRepository(private val context: Context) {

    suspend fun getPresenceHistory(): List<PresenceRecord> {
        val adminId = PreferenceHelper.getAdminId(context)
        val response = ApiClient.api.getPresenceHistory(adminId)
        return if (response.success) response.attendance_records else emptyList()
    }
}