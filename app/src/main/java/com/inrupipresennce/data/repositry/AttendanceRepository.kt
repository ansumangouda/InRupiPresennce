package com.inrupipresennce.data.repositry

import android.content.Context
import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.api.model.AttendanceResponse
import com.inrupipresennce.data.api.model.AttendanceTodayResponse
import com.inrupipresennce.data.api.model.BirthdayResponse
import com.inrupipresennce.data.api.model.EarlyBirdResponse
import com.inrupipresennce.data.api.model.LunchResponse
import com.inrupipresennce.data.api.model.OffTodayResponse
import com.inrupipresennce.data.valu.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AttendanceRepository {
    private val api = ApiClient.api

    suspend fun uploadAttendance(
        adminId: Int,
        faceDescriptor: String,
        imageFile: File?,
        lat: Double?,     // change to LAT (not latitude)
        lon: Double?      // change to LON (not longitude)
    ): AttendanceResponse? {
        val adminIdBody = adminId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val faceDescBody = faceDescriptor.toRequestBody("application/json".toMediaTypeOrNull())

        val imagePart = imageFile?.let {
            val reqFile = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", it.name, reqFile)
        }

        val latBody = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val lonBody = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.uploadAttendance(adminIdBody,faceDescBody, imagePart, latBody, lonBody)
        return if (response.isSuccessful) response.body() else null
    }
    suspend fun downloadAdminFace(context: Context, imagePath: String): File? {
        val fullUrl = Constants.BASE_URL + imagePath

        val response = ApiClient.api.downloadFile(fullUrl)
        if (!response.isSuccessful) return null

        val file = File(context.cacheDir, "admin_face_${System.currentTimeMillis()}.jpg")
        response.body()?.byteStream()?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }
    suspend fun getTodayAttendance(adminId: Int): AttendanceTodayResponse? {
        return try {
            val response = api.getTodayAttendance(adminId)

            if (response.isSuccessful) {
                response.body()
            } else {
                AttendanceTodayResponse(
                    success = false,
                    punch_in = null,
                    punch_out = null,
                    lunch_start_at = null,
                    lunch_end_at = null,
                    break_start_at = null,
                    break_end_at = null,
                    message = "Server error",
                    gradientcolor1 = null,
                    gradientcolor2 = null

                )
            }

        } catch (e: Exception) {
            AttendanceTodayResponse(
                success = false,
                punch_in = null,
                punch_out = null,
                lunch_start_at = null,
                lunch_end_at = null,
                break_start_at = null,
                break_end_at = null,
                message = e.localizedMessage ?: "Network error",
                gradientcolor1 = null,
                gradientcolor2 = null
            )
        }
    }


    suspend fun lunchBreak(adminId: Int): LunchResponse? {
        return try {
            val response = api.lunchBreak(adminId)
            if (response.isSuccessful) response.body()
            else LunchResponse(false, "Server error")
        } catch (e: Exception) {
            LunchResponse(false, e.message ?: "Network error")
        }
    }

    suspend fun getBirthdays(): BirthdayResponse? {
        return try {
            val response = api.getBirthdays()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }
    suspend fun getOffToday(): OffTodayResponse? {
        return try {
            val response = api.getOffToday()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getEarlyBirdReport(): EarlyBirdResponse? {
        return try {
            val response = api.getEarlyBirdReport()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }



}