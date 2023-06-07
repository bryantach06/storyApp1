package com.example.storyapp

import android.content.Context
import android.content.Context.MODE_PRIVATE

class LoginSession(context: Context) {

    companion object {
        private const val TOKEN_KEY = "auth_token"
    }

    private val loginSession = context.getSharedPreferences("Story App", MODE_PRIVATE)

    fun saveToken(token: String) {
        val editor = loginSession.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun passToken(): String? {
        return loginSession.getString(TOKEN_KEY, null)
    }
}