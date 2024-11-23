package com.example.chronolens.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.work.WorkManager
import com.example.chronolens.AppDatabase
import com.example.chronolens.utils.APIUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    val sharedPreferences: SharedPreferences,
    private val appDatabase: AppDatabase,
    val context: Context
) {

    suspend fun checkLogin(): Boolean {
        return APIUtils.checkLogin(sharedPreferences)
    }

    suspend fun apiLogin(server: String, username: String, password: String): Int? {
        return APIUtils.login(sharedPreferences, server, username, password)

    }
 
    suspend fun logout() {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().clear().apply()
            appDatabase.remoteAssetDao().deleteAllRemoteAssets()
            WorkManager.getInstance(context).cancelAllWork()
        }
    }

}

