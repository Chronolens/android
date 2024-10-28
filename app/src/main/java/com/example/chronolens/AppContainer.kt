package com.example.chronolens

import android.content.ContentResolver
import android.content.Context
import com.example.chronolens.repositories.MediaGridRepository
import com.example.chronolens.repositories.UserRepository


interface AppContainer {
    val mediaGridRepository: MediaGridRepository
    val userRepository: UserRepository
}

class ChronoLensAppContainer(private val context: Context) : AppContainer {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }
    private val apiServiceClient: APIService = APIService()
    private val contentResolver: ContentResolver = context.contentResolver

    override val mediaGridRepository: MediaGridRepository by lazy {
        MediaGridRepository(
            database.checksumDao(),
            database.remoteAssetDao(),
            apiServiceClient,
            sharedPreferences,
            contentResolver
        )
    }

    override val userRepository: UserRepository by lazy {
        UserRepository(apiServiceClient,sharedPreferences)
    }


}