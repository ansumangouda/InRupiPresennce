package com.inrupipresennce.data.model.request

data class UpdateTaskRequest(
    val admin_id: Int,
    val status: String,
    val notes: String
)