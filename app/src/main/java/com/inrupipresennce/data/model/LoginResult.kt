package com.inrupipresennce.data.model

import com.google.gson.annotations.SerializedName

data class LoginResult(
    val success: Boolean,
    val message: String,
    val id: Int = 0,
    val name: String = "",
    val image: String? = null,
    @SerializedName("OFFICE_LAT")
    val officeLat: String = "",

    @SerializedName("OFFICE_LON")
    val officeLon: String = "",

    @SerializedName("OFFICE_RADIUS_METERS")
    val officeRadius: String = ""
)