package com.example.chronolens.repositories

import android.content.SharedPreferences
import com.example.chronolens.utils.APIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    val sharedPreferences: SharedPreferences,
) {

    suspend fun checkLogin(){
        APIUtils.checkLogin(sharedPreferences)
    }

    suspend fun apiLogin(server: String, username: String, password: String): Int? {
        return withContext(Dispatchers.IO) {
            APIUtils.login(sharedPreferences, server,username, password)
        }
    }

}