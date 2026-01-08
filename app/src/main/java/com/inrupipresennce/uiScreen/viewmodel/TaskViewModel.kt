package com.inrupipresennce.uiScreen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.model.CreateTaskResponse
import com.inrupipresennce.data.model.TaskData
import com.inrupipresennce.data.model.request.CreateTaskRequest
import com.inrupipresennce.data.model.request.UpdateTaskRequest
import com.inrupipresennce.data.repositry.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _tasks = MutableStateFlow<List<TaskData>>(emptyList())
    val tasks: StateFlow<List<TaskData>> = _tasks

    private val _createTaskResult = MutableStateFlow<CreateTaskResponse?>(null)
    val createTaskResult: StateFlow<CreateTaskResponse?> = _createTaskResult

    private val _updateTaskResult = MutableStateFlow<CreateTaskResponse?>(null)
    val updateTaskResult: StateFlow<CreateTaskResponse?> = _updateTaskResult

    fun getTasks(adminId: Int) {
        viewModelScope.launch {
            try {
                val taskResponse = repository.getTasks(adminId)
                _tasks.value = taskResponse.data
                Log.d("TaskViewModel", "Tasks loaded: ${taskResponse.data}")
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error fetching tasks", e)
            }
        }
    }

    fun createTask(request: CreateTaskRequest) {
        viewModelScope.launch {
            try {
                val response = repository.createTask(request)
                _createTaskResult.value = response
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error creating task", e)
            }
        }
    }

    fun onTaskCreated() {
        _createTaskResult.value = null
    }

    fun updateTask(taskId: Int, request: UpdateTaskRequest) {
        viewModelScope.launch {
            try {
                val response = repository.updateTask(taskId, request)
                _updateTaskResult.value = response
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error updating task", e)
            }
        }
    }

    fun onTaskUpdated() {
        _updateTaskResult.value = null
    }
}