package com.example.chronolens

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.chronolens.database.Checksum
import com.example.chronolens.database.ChecksumDao
import com.example.chronolens.database.RemoteAssetDao
import com.example.chronolens.database.RemoteAssetDb
import kotlinx.coroutines.InternalCoroutinesApi

@Database(entities = [Checksum::class, RemoteAssetDb::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checksumDao(): ChecksumDao
    abstract fun remoteAssetDao(): RemoteAssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
