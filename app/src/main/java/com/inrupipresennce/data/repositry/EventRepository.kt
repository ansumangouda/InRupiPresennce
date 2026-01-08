package com.inrupipresennce.data.repositry

import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.model.EventResponse

class EventRepository {
    suspend fun getEvents(): EventResponse {
        return ApiClient.api.getEvents()
    }
}