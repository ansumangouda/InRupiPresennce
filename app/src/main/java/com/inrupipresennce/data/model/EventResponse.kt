package com.inrupipresennce.data.model

import com.google.gson.annotations.SerializedName

data class EventResponse(
    @SerializedName("status") val status: Boolean?,
    @SerializedName("count") val count: Int?,
    @SerializedName("data") val data: List<EventData>?
)

data class EventData(
    @SerializedName("title") val title: String?,
    @SerializedName("event_type") val eventType: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("event_date") val eventDate: String?,
    @SerializedName("event_time") val eventTime: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("applied_by") val appliedBy: String?
)
