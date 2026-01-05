package com.inrupipresennce.data.repositry

import android.content.Context
import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.model.LeaveResponse
import com.inrupipresennce.data.model.request.ApplyLeaveRequest
import com.inrupipresennce.utils.PreferenceHelper

class LeaveRepository(private val context: Context) {

    private val adminId = PreferenceHelper.getAdminId(context)

    suspend fun fetchLeaves(year: Int?): LeaveResponse {
        return ApiClient.api.getLeaves(
            adminId = adminId,
            year = year?.toString()
        )
    }

    suspend fun applyLeave(
        from: String,
        to: String,
        type: String,
        reason: String?
    ): LeaveResponse {
        return ApiClient.api.applyLeave(
            ApplyLeaveRequest(
                admin_id = adminId,
                from_date = from,
                to_date = to,
                leave_type = type,
                reason = reason
            )
        )
    }

    suspend fun updateLeave(
        leaveId: Int,
        from: String,
        to: String,
        type: String?,
        reason: String?
    ): LeaveResponse {
        return ApiClient.api.updateLeave(
            leaveId,
            ApplyLeaveRequest(adminId, from, to, type ?: "casual", reason)
        )
    }
}
