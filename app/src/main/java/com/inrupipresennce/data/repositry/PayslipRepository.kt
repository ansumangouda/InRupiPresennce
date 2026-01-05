package com.inrupipresennce.data.repositry

import android.content.Context
import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.model.PayslipResponse
import com.inrupipresennce.utils.PreferenceHelper

class PayslipRepository(private val context: Context) {

    suspend fun getPayslips(): PayslipResponse {
        val adminId = PreferenceHelper.getAdminId(context)
        return ApiClient.api.getPayslips(adminId)
    }
}
