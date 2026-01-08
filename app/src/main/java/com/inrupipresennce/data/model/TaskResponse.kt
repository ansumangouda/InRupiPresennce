package com.inrupipresennce.data.model

import com.google.gson.annotations.SerializedName

data class TaskResponse(
    val data: List<TaskData>
)

data class TaskData(
    val id: Int,
    val title: String?,
    val description: String?,
    val notes: String?,
    val status: String?,
    @SerializedName("assigned_at") val assignedAt: String?,
    @SerializedName("assigned_by") val assignedBy: AssignedUser?,
    @SerializedName("assigned_to") val assignedTo: AssignedUser?,
    @SerializedName("due_date") val dueDate: String?,
    val priority: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    @SerializedName("deleted_at") val deletedAt: String?
)

data class AssignedUser(
    val id: Int,
    val name: String?
)