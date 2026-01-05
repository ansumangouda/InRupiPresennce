package com.inrupipresennce.data.model

data class LeaveResponse(
    val success: Boolean,
    val admin_id: Int?,
    val year: Int?,
    val leave_summary: LeaveSummary?,
    val total_leaves: Int?,
    val leaves: List<LeaveItem>?,
    val message: String?
)

data class LeaveSummary(
    val paid_added: Int,
    val carry_forward: Int,
    val paid_used: Int,
    val unpaid_used: Int
)

data class LeaveItem(
    val id: Int,
    val admin_id: Int,
    val from_date: String,
    val to_date: String,
    val total_days: Int,
    val leave_type: String,
    val pay_type: String,
    val status: String,
    val reason: String?
)