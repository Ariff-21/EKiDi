package com.example.ekidi.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "ekidi_session"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_ROLE = "user_role"
        const val KEY_USER_LEVEL = "user_level"
        const val KEY_USER_POINTS = "user_points"
        const val KEY_USER_AVATAR = "user_avatar"
        const val KEY_AUTH_TOKEN = "auth_token"
    }

    fun saveLoginSession(
        userId: Int,
        userName: String,
        userRole: String,
        authToken: String,
        level: Int = 1,
        points: Int = 0
    ) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_ROLE, userRole)
            putString(KEY_AUTH_TOKEN, authToken)
            putInt(KEY_USER_LEVEL, level)
            putInt(KEY_USER_POINTS, points)
            apply()
        }
    }

    fun logout() { prefs.edit().clear().apply() }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "Pengguna") ?: "Pengguna"
    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "anak") ?: "anak"
    fun getAuthToken(): String = prefs.getString(KEY_AUTH_TOKEN, "") ?: ""
    fun getUserLevel(): Int = prefs.getInt(KEY_USER_LEVEL, 1)
    fun getUserPoints(): Int = prefs.getInt(KEY_USER_POINTS, 0)
    fun getUserAvatar(): String = prefs.getString(KEY_USER_AVATAR, "avatar_default") ?: "avatar_default"

    fun updatePoints(newPoints: Int) { prefs.edit().putInt(KEY_USER_POINTS, newPoints).apply() }
    fun updateLevel(newLevel: Int) { prefs.edit().putInt(KEY_USER_LEVEL, newLevel).apply() }
    fun updateAvatar(avatarKey: String) { prefs.edit().putString(KEY_USER_AVATAR, avatarKey).apply() }

    fun isChild(): Boolean = getUserRole() == "anak"
    fun isParent(): Boolean = getUserRole() == "orang_tua"
    fun isTeacher(): Boolean = getUserRole() == "guru"
}