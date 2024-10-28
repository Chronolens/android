package com.example.chronolens.repositories

import android.content.SharedPreferences
import com.example.chronolens.APIService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val apiServiceClient: APIService,
    val sharedPreferences: SharedPreferences,
) {

    fun checkLogin(){
        apiServiceClient.checkLogin(sharedPreferences)
    }

    suspend fun apiLogin(server: String, username: String, password: String): Int? {
        return withContext(Dispatchers.IO) {
            apiServiceClient.login(sharedPreferences, server,username, password)
        }
    }

}