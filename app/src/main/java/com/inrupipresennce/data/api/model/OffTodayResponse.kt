package com.inrupipresennce.data.api.model

data class OffTodayResponse(
    val status: Boolean,
    val message: String,
    val count: Int,
    val data: List<OffTodayUser>
)

data class OffTodayUser(
    val id: Int,
    val name: String,
    val image: String?
)
