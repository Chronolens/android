package com.example.chronolens.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checksums")
data class Checksum(
    @PrimaryKey val localId: Long,
    val checksum: String
)

@Entity(tableName = "remote_assets")
data class RemoteAssetDb(
    @PrimaryKey val remoteId: String,
    val checksum: String,
    val timestamp: Long
)
