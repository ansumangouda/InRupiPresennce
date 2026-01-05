package com.inrupipresennce.data.model

import com.google.gson.annotations.SerializedName

data class PayslipResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("count") val count: Int?,
    @SerializedName("data") val data: List<PayslipData>?,
    @SerializedName("error") val error: List<String>?
)

data class PayslipData(
    @SerializedName("id") val id: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("month_name") val monthName: String,
    @SerializedName("year") val year: Int,
    @SerializedName("gross_salary") val grossSalary: String,
    @SerializedName("total_deductions") val totalDeductions: String,
    @SerializedName("net_salary") val netSalary: String,
    @SerializedName("status") val status: String,
    @SerializedName("generated_at") val generatedAt: String,
    @SerializedName("pdf_url") val pdfUrl: String
)
