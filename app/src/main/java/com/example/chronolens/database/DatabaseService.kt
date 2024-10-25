package com.example.chronolens.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface ChecksumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecksum(checksum: Checksum)

    @Query("SELECT * FROM checksums WHERE localId = :localId LIMIT 1")
    suspend fun getChecksum(localId: String): Checksum?

    @Query("SELECT * FROM checksums WHERE localId IN (:ids)")
    suspend fun getChecksumsFromList(ids: List<String>): List<Checksum>
}

@Dao
interface RemoteAssetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRemoteAssets(remoteAssets: List<RemoteAssetDb>)

    @Query("DELETE FROM remote_assets WHERE remoteId IN (:remoteIds)")
    suspend fun deleteRemoteAssets(remoteIds: List<String>)

    @Query("SELECT * FROM remote_assets")
    suspend fun getRemoteAssets(): List<RemoteAssetDb>
}