package com.inrupipresennce.data.api.model

data class PresenceResponse(
    val success: Boolean,
    val attendance_records: List<PresenceRecord>
)

data class PresenceRecord(
    val id: Int,
    val punch_in_image: String?,
    val punch_in_at: String,
    val punch_out_at: String?,
    val punchinlatitude: String?,
    val punchinlongitude: String?,
    val punchoutlatitude: String?,
    val punchoutlongitude: String?
)
