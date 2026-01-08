package com.inrupipresennce.data.model.request

data class CreateTaskRequest(
    val admin_id: Int,
    val title: String,
    val description: String,
    val assigned_to: Int,
    val priority: String,
    val due_date: String
)