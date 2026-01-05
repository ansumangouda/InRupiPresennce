package com.inrupipresennce.data.model

import com.google.gson.annotations.SerializedName

data class TeamMatesResponse(
    val status: Boolean,
    val message: String,
    val manager: Manager,
    @SerializedName("team_count")
    val teamCount: Int,
    val team: List<TeamMember>
)

data class Manager(
    val id: Int,
    val name: String,
    val phone: String,
    val role: String,
    val image: String,
    val details: AdminDetails
)

data class TeamMember(
    val id: Int,
    val name: String,
    val phone: String,
    val role: String,
    val details: AdminDetails
)

data class AdminDetails(
    val id: Int,
    @SerializedName("admin_id")
    val adminId: Int,
    @SerializedName("emp_id")
    val empId: String,
    val token: String?,
    @SerializedName("face_descriptor")
    val faceDescriptor: String,
    @SerializedName("alt_phone")
    val altPhone: String?,
    @SerializedName("current_address")
    val currentAddress: String?,
    @SerializedName("blood_grp")
    val bloodGrp: String?,
    val dob: String?,
    @SerializedName("joining_date")
    val joiningDate: String?,
    @SerializedName("aadharcard_no")
    val aadharcardNo: String?,
    val pancardno: String?,
    val designation: String?,
)
