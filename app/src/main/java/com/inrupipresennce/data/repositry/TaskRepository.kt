package com.inrupipresennce.data.repositry

import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.api.ApiService
import com.inrupipresennce.data.model.CreateTaskResponse
import com.inrupipresennce.data.model.TaskResponse
import com.inrupipresennce.data.model.request.CreateTaskRequest
import com.inrupipresennce.data.model.request.UpdateTaskRequest

class TaskRepository {
    private val apiService: ApiService = ApiClient.api

    suspend fun getTasks(adminId: Int): TaskResponse {
        return apiService.getTasks(adminId)
    }

    suspend fun createTask(request: CreateTaskRequest): CreateTaskResponse {
        return apiService.createTask(request)
    }

    suspend fun updateTask(taskId: Int, request: UpdateTaskRequest): CreateTaskResponse {
        return apiService.updateTask(taskId, request)
    }
}