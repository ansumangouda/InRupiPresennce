package com.inrupipresennce.data.model

data class CreateTaskResponse(
    val message: String,
    val task: Task
)

data class Task(
    val id: Int,
    val status: String
)