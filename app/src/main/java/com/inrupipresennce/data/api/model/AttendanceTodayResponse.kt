package com.inrupipresennce.data.api.model

data class AttendanceTodayResponse(
    val success: Boolean,
    val punch_in: String?,
    val punch_out: String?,
    val lunch_start_at: String?,
    val lunch_end_at: String?,
    val break_start_at: String?,
    val break_end_at: String?,
    val message: String?,
    val gradientcolor1 : String?,
    val gradientcolor2 : String?,
)
