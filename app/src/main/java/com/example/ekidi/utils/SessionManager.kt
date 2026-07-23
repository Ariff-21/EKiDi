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
        
        // Badge Keys
        const val KEY_BADGE_1 = "badge_1" // Pemula
        const val KEY_BADGE_2 = "badge_2" // Penjelajah
        const val KEY_BADGE_3 = "badge_3" // Juara Game
        const val KEY_BADGE_4 = "badge_4" // Misi Master
        const val KEY_BADGE_5 = "badge_5" // Literasi Pro
        const val KEY_BADGE_6 = "badge_6" // Bintang EKiDi
        const val KEY_BADGE_7 = "badge_7" // Sniper Jawaban
        const val KEY_BADGE_8 = "badge_8" // Pejuang Streak
        const val KEY_BADGE_9 = "badge_9" // Master Runner
        const val KEY_BADGE_10 = "badge_10" // Kolektor Bintang
        const val KEY_BADGE_11 = "badge_11" // Jenius Digital
        const val KEY_BADGE_12 = "badge_12" // Legenda EKiDi
        
        // Counter Keys for Badges
        const val KEY_GAME_PLAY_COUNT = "game_play_count"
        const val KEY_MISSION_CLAIM_COUNT = "mission_claim_count"
        const val KEY_PERFECT_QUIZ_COUNT = "perfect_quiz_count"
        const val KEY_COMPLETED_TOPICS = "completed_topics" // Format: "1,2,3"
        
        // Mission Keys
        const val MISI_HARIAN_1_STATUS = "misi_harian_1_status" // 0: belum, 1: selesai, 2: diklaim
        const val MISI_HARIAN_2_STATUS = "misi_harian_2_status"
        const val MISI_HARIAN_3_STATUS = "misi_harian_3_status"
        const val MISI_MINGGUAN_STATUS = "misi_mingguan_status"
        const val MISI_SPESIAL_STATUS = "misi_spesial_status"
        const val MISI_MINGGUAN_PROGRESS = "misi_mingguan_progress"
        const val KEY_LAST_WEEKLY_RESET = "last_weekly_reset"
        const val MISI_SPESIAL_PROGRESS = "misi_spesial_progress"
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

    fun setBadgeStatus(key: String, isEarned: Boolean) {
        prefs.edit().putBoolean(key, isEarned).apply()
        updateTotalBadgeCount()
    }

    fun getBadgeStatus(key: String): Boolean = prefs.getBoolean(key, false)

    private fun updateTotalBadgeCount() {
        var count = 0
        if (getBadgeStatus(KEY_BADGE_1)) count++
        if (getBadgeStatus(KEY_BADGE_2)) count++
        if (getBadgeStatus(KEY_BADGE_3)) count++
        if (getBadgeStatus(KEY_BADGE_4)) count++
        if (getBadgeStatus(KEY_BADGE_5)) count++
        if (getBadgeStatus(KEY_BADGE_6)) count++
        if (getBadgeStatus(KEY_BADGE_7)) count++
        if (getBadgeStatus(KEY_BADGE_8)) count++
        if (getBadgeStatus(KEY_BADGE_9)) count++
        if (getBadgeStatus(KEY_BADGE_10)) count++
        if (getBadgeStatus(KEY_BADGE_11)) count++
        if (getBadgeStatus(KEY_BADGE_12)) count++
        prefs.edit().putInt(KEY_TOTAL_BADGE, count).apply()
    }

    // Incremental badge progress
    fun incrementGamePlayCount() {
        val current = prefs.getInt(KEY_GAME_PLAY_COUNT, 0)
        prefs.edit().putInt(KEY_GAME_PLAY_COUNT, current + 1).apply()
    }
    fun getGamePlayCount(): Int = prefs.getInt(KEY_GAME_PLAY_COUNT, 0)

    fun incrementMissionClaimCount() {
        val current = prefs.getInt(KEY_MISSION_CLAIM_COUNT, 0)
        prefs.edit().putInt(KEY_MISSION_CLAIM_COUNT, current + 1).apply()
    }
    fun getMissionClaimCount(): Int = prefs.getInt(KEY_MISSION_CLAIM_COUNT, 0)

    fun incrementPerfectQuizCount() {
        val current = prefs.getInt(KEY_PERFECT_QUIZ_COUNT, 0)
        prefs.edit().putInt(KEY_PERFECT_QUIZ_COUNT, current + 1).apply()
    }
    fun getPerfectQuizCount(): Int = prefs.getInt(KEY_PERFECT_QUIZ_COUNT, 0)

    fun markTopicCompleted(topikId: Int) {
        val completed = prefs.getString(KEY_COMPLETED_TOPICS, "") ?: ""
        if (!completed.contains(topikId.toString())) {
            val newList = if (completed.isEmpty()) topikId.toString() else "$completed,$topikId"
            prefs.edit().putString(KEY_COMPLETED_TOPICS, newList).apply()
        }
    }
    fun getCompletedTopicsCount(): Int {
        val completed = prefs.getString(KEY_COMPLETED_TOPICS, "") ?: ""
        return if (completed.isEmpty()) 0 else completed.split(",").size
    }

    fun setMisiStatus(key: String, status: Int) {
        prefs.edit().putInt(key, status).apply()
    }

    fun getMisiStatus(key: String): Int = prefs.getInt(key, 0)

    fun updateMisiMingguanProgress(delta: Int) {
        val current = prefs.getInt(MISI_MINGGUAN_PROGRESS, 0)
        prefs.edit().putInt(MISI_MINGGUAN_PROGRESS, current + delta).apply()
    }

    fun getMisiMingguanProgress(): Int = prefs.getInt(MISI_MINGGUAN_PROGRESS, 0)

    fun getLastWeeklyReset(): Long = prefs.getLong(KEY_LAST_WEEKLY_RESET, 0L)
    fun saveLastWeeklyReset(timestamp: Long) { prefs.edit().putLong(KEY_LAST_WEEKLY_RESET, timestamp).apply() }

    fun setMisiMingguanProgress(progress: Int) {
        prefs.edit().putInt(MISI_MINGGUAN_PROGRESS, progress).apply()
    }

    fun setMisiSpesialProgress(progress: Int) {
        prefs.edit().putInt(MISI_SPESIAL_PROGRESS, progress).apply()
    }
    fun getMisiSpesialProgress(): Int = prefs.getInt(MISI_SPESIAL_PROGRESS, 0)
}