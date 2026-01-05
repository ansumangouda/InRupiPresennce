package com.inrupipresennce.data.model.request

data class ApplyLeaveRequest(
    val admin_id: Int,
    val from_date: String,
    val to_date: String,
    val leave_type: String,
    val reason: String?
)