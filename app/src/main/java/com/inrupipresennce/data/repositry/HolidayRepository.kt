package com.inrupipresennce.data.repositry

import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.api.ApiService
import com.inrupipresennce.data.model.Holiday


class HolidayRepository {
    private val apiService: ApiService = ApiClient.api

    suspend fun getHolidays(): Map<String, List<Holiday>> {
        return apiService.getHolidays()
    }
}