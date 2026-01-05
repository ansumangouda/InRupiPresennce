package com.inrupipresennce.data.model

import com.google.gson.annotations.SerializedName

data class AttendanceRequest(
    @SerializedName("admin_id") val adminId: Int,
    val year: Int? = null,
    val month: Int? = null
)