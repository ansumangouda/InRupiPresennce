
package com.inrupipresennce.data.api.model

import com.google.gson.annotations.SerializedName

data class BirthdayResponse(
    val status: Boolean,
    val message: String,
    val count: BirthdayCount?,
    val today: List<BirthdayPerson>?,
    val upcoming: List<BirthdayPerson>?
)

data class BirthdayCount(
    val today: Int,
    val upcoming: Int
)

data class BirthdayPerson(
    val id: Int,
    val name: String,
    val image: String,
    val dob: String
)
data class Teammate(
    val name: String,
    val role: String,
    val imageUrl: String?,
    val birthdayText: String
)
