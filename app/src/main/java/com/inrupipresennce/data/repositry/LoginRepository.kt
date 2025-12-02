package com.inrupipresennce.data.repositry

import android.content.Context
import com.inrupipresennce.data.api.ApiClient
import com.inrupipresennce.data.api.model.LoginResult
import com.inrupipresennce.data.api.model.request.LoginRequest
import com.inrupipresennce.data.valu.Constants
import com.inrupipresennce.utils.PreferenceHelper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class LoginRepository(private val context: Context) {

    suspend fun login(phone: String): LoginResult = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.api.login(LoginRequest(phone))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    saveLoginData(body)
                    body
                } else {
                    LoginResult(false, body?.message ?: "No admin found with this phone number.")
                }
            } else {
                val errorJson = response.errorBody()?.string()
                val errorMessage = try {
                    val json = JSONObject(errorJson ?: "{}")

                    // Prefer "message" field; fallback to first item in "messages"
                    when {
                        json.has("message") -> json.getString("message")
                        json.has("messages") -> {
                            val arr = json.getJSONArray("messages")
                            if (arr.length() > 0) arr.getString(0) else response.message()
                        }
                        else -> response.message()
                    }

                } catch (e: Exception) {
                    response.message()
                }

                LoginResult(false, errorMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            LoginResult(false, "Network error: ${e.message}")
        }
    }

    private fun saveLoginData(result: LoginResult) {
        PreferenceHelper.saveLoginData(
            context = context,
            adminId = result.id,
            name = result.name,
            image = result.image,
            officeLat = result.officeLat,
            officeLon = result.officeLon,
            officeRadius = result.officeRadius

        )
    }

    fun checkLogin(): Pair<Boolean, Int?> {
        val isLoggedIn = PreferenceHelper.isLoggedIn(context)
        val adminId = PreferenceHelper.getAdminId(context)
        return Pair(isLoggedIn, adminId)
    }
    suspend fun downloadAdminFace(context: Context, imagePath: String): File? {
        val fullUrl = Constants.BASE_URL + imagePath
        return try {
            val response = ApiClient.api.downloadFile(fullUrl)
            if (!response.isSuccessful) return null

            val file = File(context.cacheDir, "stored_face_${System.currentTimeMillis()}.jpg")
            response.body()?.byteStream()?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
