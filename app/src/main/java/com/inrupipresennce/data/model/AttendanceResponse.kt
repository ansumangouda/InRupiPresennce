package com.inrupipresennce.data.model

data class AttendanceResponse(
    val success: Boolean,
    val message: String,
    val attendance_type: String?,
    val admin_id: Int?,
    val name: String?,
    val distance: Double?,
    val status_code : Int?,
    val messages: List<String>? = null
)

