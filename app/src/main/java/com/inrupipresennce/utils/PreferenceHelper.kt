package com.inrupipresennce.utils

import android.content.Context
import androidx.core.content.edit

object PreferenceHelper {

    private const val PREFS_NAME = "login_prefs"

    fun setFaceVerified(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean("faceVerified", value) }
    }

    fun isFaceVerified(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("faceVerified", false)
    }

    fun saveLoginData(
        context: Context,
        adminId: Int,
        name: String?,
        image: String?,
        officeLat: String?,
        officeLon: String?,
        officeRadius: String?
    ) {
        val expireAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit {
            putBoolean("isLoggedIn", true)
            putInt("adminId", adminId)
            putString("name", name)
            putString("image", image)
            putLong("expireAt", expireAt)
            putString("office_lat", officeLat)
            putString("office_lon", officeLon)
            putString("office_radius", officeRadius)
        }
    }

    fun getAdminId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("adminId", 0)
    }

    fun getImagePath(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("image", null)
    }

    fun getOfficeLat(context: Context): Double {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("office_lat", "0.0")
            ?.toDoubleOrNull() ?: 0.0
    }

    fun getOfficeLon(context: Context): Double {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("office_lon", "0.0")
            ?.toDoubleOrNull() ?: 0.0
    }

    fun getOfficeRadius(context: Context): Double {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("office_radius", "20.0")
            ?.toDoubleOrNull() ?: 20.0
    }

    fun saveGradientColors(context: Context, c1: String?, c2: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString("gradient_color_1", c1)
            putString("gradient_color_2", c2)
        }
    }

    fun getGradientColors(context: Context): Pair<String?, String?> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val c1 = prefs.getString("gradient_color_1", "0xFFDAEA29")
        val c2 = prefs.getString("gradient_color_2", "0xFF5D991D")
        return Pair(c1, c2)
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val expireAt = prefs.getLong("expireAt", 0L)
        return isLoggedIn && System.currentTimeMillis() < expireAt
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { clear() }
    }
}
