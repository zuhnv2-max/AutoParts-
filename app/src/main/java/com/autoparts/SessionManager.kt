package com.autoparts

import android.content.Context
import android.content.SharedPreferences
import com.autoparts.data.entity.User
import com.google.gson.Gson

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "AutoPartsSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER = "currentUser"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_PHONE = "userPhone"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_ROLE = "userRole"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveCurrentUser(user: User) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER, gson.toJson(user))
        editor.putInt(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putString(KEY_USER_PHONE, user.phone)
        editor.putString(KEY_USER_NAME, user.name)
        editor.putString(KEY_USER_ROLE, user.role)
        editor.apply()
    }

    fun getCurrentUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }

    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun isAdmin(): Boolean {
        val role = sharedPreferences.getString(KEY_USER_ROLE, "user") ?: "user"
        return role == "admin"
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }

    fun getUserId(): Int = sharedPreferences.getInt(KEY_USER_ID, -1)
    fun getUserEmail(): String = sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserPhone(): String = sharedPreferences.getString(KEY_USER_PHONE, "") ?: ""
    fun getUserName(): String = sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
    fun getUserRole(): String = sharedPreferences.getString(KEY_USER_ROLE, "user") ?: "user"
}