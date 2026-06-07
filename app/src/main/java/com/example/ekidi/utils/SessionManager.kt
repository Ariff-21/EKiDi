package com.example.ekidi.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ekidi_session"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_LEVEL = "user_level"
        const val KEY_USER_POINTS = "user_points"
        const val KEY_USER_AVATAR = "user_avatar"
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_TOTAL_BINTANG = "total_bintang"
        const val KEY_TOTAL_BADGE = "total_badge"
        const val KEY_TOTAL_PEMBELAJARAN = "total_pembelajaran"
        const val KEY_STREAK = "streak"
        const val KEY_LAST_RESET_DATE = "last_reset_date"
        
        // Mission Keys
        const val MISI_HARIAN_1_STATUS = "misi_harian_1_status" // 0: belum, 1: selesai, 2: diklaim
        const val MISI_HARIAN_2_STATUS = "misi_harian_2_status"
        const val MISI_HARIAN_3_STATUS = "misi_harian_3_status"
        const val MISI_MINGGUAN_STATUS = "misi_mingguan_status"
        const val MISI_SPESIAL_STATUS = "misi_spesial_status"
        const val MISI_MINGGUAN_PROGRESS = "misi_mingguan_progress"
    }

    fun saveLoginSession(
        userId: Int,
        userName: String,
        authToken: String,
        level: Int = 1,
        points: Int = 0,
        avatar: String = "🐶",
        totalBintang: Int = 0,
        totalBadge: Int = 1,
        totalPembelajaran: Int = 0,
        streak: Int = 0
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_AUTH_TOKEN, authToken)
            putInt(KEY_USER_LEVEL, level)
            putInt(KEY_USER_POINTS, points)
            putString(KEY_USER_AVATAR, avatar)
            putInt(KEY_TOTAL_BINTANG, totalBintang)
            putInt(KEY_TOTAL_BADGE, totalBadge)
            putInt(KEY_TOTAL_PEMBELAJARAN, totalPembelajaran)
            putInt(KEY_STREAK, streak)
            apply()
        }
    }

    fun logout() { prefs.edit().clear().apply() }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Pengguna") ?: "Pengguna"
    fun getAuthToken(): String = prefs.getString(KEY_AUTH_TOKEN, "") ?: ""
    fun getUserLevel(): Int = prefs.getInt(KEY_USER_LEVEL, 1)
    fun getUserPoints(): Int = prefs.getInt(KEY_USER_POINTS, 0)
    fun getUserAvatar(): String = prefs.getString(KEY_USER_AVATAR, "🐶") ?: "🐶"
    fun getTotalBintang(): Int = prefs.getInt(KEY_TOTAL_BINTANG, 0)
    fun getTotalBadge(): Int = prefs.getInt(KEY_TOTAL_BADGE, 1)
    fun getTotalPembelajaran(): Int = prefs.getInt(KEY_TOTAL_PEMBELAJARAN, 0)
    fun getStreak(): Int = prefs.getInt(KEY_STREAK, 0)
    
    fun getLastResetDate(): String = prefs.getString(KEY_LAST_RESET_DATE, "") ?: ""
    fun saveLastResetDate(date: String) { prefs.edit().putString(KEY_LAST_RESET_DATE, date).apply() }

    fun checkDailyReset() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("id-ID"))
        val today = sdf.format(Date())
        val lastReset = getLastResetDate()

        if (today != lastReset) {
            // Reset misi harian lokal
            setMisiStatus(MISI_HARIAN_1_STATUS, 0)
            setMisiStatus(MISI_HARIAN_2_STATUS, 0)
            setMisiStatus(MISI_HARIAN_3_STATUS, 0)
            saveLastResetDate(today)
        }
    }

    fun updatePoints(newPoints: Int) { prefs.edit().putInt(KEY_USER_POINTS, newPoints).apply() }
    fun updateLevel(newLevel: Int) { prefs.edit().putInt(KEY_USER_LEVEL, newLevel).apply() }
    fun updateAvatar(avatarKey: String) { prefs.edit().putString(KEY_USER_AVATAR, avatarKey).apply() }

    fun setMisiStatus(key: String, status: Int) {
        prefs.edit().putInt(key, status).apply()
    }

    fun getMisiStatus(key: String): Int = prefs.getInt(key, 0)

    fun updateMisiMingguanProgress(delta: Int) {
        val current = prefs.getInt(MISI_MINGGUAN_PROGRESS, 0)
        prefs.edit().putInt(MISI_MINGGUAN_PROGRESS, current + delta).apply()
    }

    fun getMisiMingguanProgress(): Int = prefs.getInt(MISI_MINGGUAN_PROGRESS, 0)
}