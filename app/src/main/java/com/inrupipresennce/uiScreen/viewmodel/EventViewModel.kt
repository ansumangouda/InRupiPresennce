package com.inrupipresennce.uiScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inrupipresennce.data.model.EventData
import com.inrupipresennce.data.repositry.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel(private val repository: EventRepository) : ViewModel() {

    private val _events = MutableStateFlow<List<EventData>>(emptyList())
    val events: StateFlow<List<EventData>> = _events

    fun getEvents() {
        viewModelScope.launch {
            try {
                val response = repository.getEvents()
                _events.value = response.data ?: emptyList()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}