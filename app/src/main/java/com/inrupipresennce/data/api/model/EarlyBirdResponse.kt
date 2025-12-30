package com.inrupipresennce.data.api.model

data class EarlyBirdResponse(
    val status: Boolean,
    val message: String,
    val timezone: String,
    val today_early_birds: List<TodayEarlyBird>,
    val monthly_ranking: List<MonthlyEarlyBird>
)

data class TodayEarlyBird(
    val admin_id: Int,
    val name: String,
    val image: String?,        // 👈 ADD THIS
    val punch_in_time: String
)

data class MonthlyEarlyBird(
    val admin_id: Int,
    val name: String,
    val avg_punch_in: String,
    val image: String?,        // 👈 ADD THIS
    val valid_working_days: Int,
    val rank: Int,
    val early_days_count: Int,

    )
data class EarlyTeammate(
    val name: String,
    val role: String,
    val subText: String,
    val imageUrl: String?      // 👈 ADD THIS
)
