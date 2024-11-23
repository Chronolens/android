package com.example.chronolens.utils

import android.provider.MediaStore
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.repositories.MediaGridRepository

// TODO: we can get thumbnail bytes from exif or mediaStore directly
class SyncManager(
    val mediaGridRepository: MediaGridRepository
) {
    suspend fun getRemoteAssets(): List<RemoteMedia> {
        val lastSync = mediaGridRepository.sharedPreferences.getLong(Prefs.LAST_SYNC, 0L)

        var remoteAssets: List<RemoteMedia> = if (lastSync == 0L) {
            // Full Sync
            val remote = mediaGridRepository.apiSyncFullRemote()
            mediaGridRepository.dbUpsertRemoteAssets(remote)
            remote
        } else {
            // Partial Sync
            val (uploaded, deleted) = mediaGridRepository.apiSyncPartialRemote(lastSync)
            mediaGridRepository.dbUpsertRemoteAssets(uploaded)
            mediaGridRepository.dbDeleteRemoteAssets(deleted)
            mediaGridRepository.dbGetRemoteAssets()
        }

        remoteAssets = remoteAssets.sortedByDescending { it.timestamp }
        return remoteAssets
    }

    fun getLocalAssets(): List<LocalMedia> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,           // MediaStore ID
            MediaStore.Images.Media.DATA,          // File path
            MediaStore.Images.Media.DATE_TAKEN,    // Timestamp (date taken)
            MediaStore.Images.Media.DATE_MODIFIED,  // Last modified timestamp
            MediaStore.Images.Media.MIME_TYPE
        )
        val cursor = mediaGridRepository.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        )

        val localMediaInfo = mutableListOf<LocalMedia>()


        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateTakenColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idColumn)
                val path = cursor.getString(pathColumn)
                val dateTaken =
                    if (dateTakenColumn != -1) it.getLong(dateTakenColumn) else null // Get the DATE_TAKEN value, can be null
                val mimeType = cursor.getString(mimeTypeColumn)

                val finalTimestamp: Long = if (dateTaken == null || dateTaken == 0L) {
                    it.getLong(dateModifiedColumn) * 1000
                } else {
                    dateTaken
                }

                localMediaInfo.add(
                    LocalMedia(
                        remoteId = null,
                        id = id,
                        path = path,
                        mimeType = mimeType,
                        checksum = null,
                        timestamp = finalTimestamp
                    )
                )
            }
        }

        localMediaInfo.sortByDescending { it.timestamp }
        return localMediaInfo
    }

    fun mergeAssets(local: List<LocalMedia>, remote: List<RemoteMedia>): List<MediaAsset> {
        val mediaAssets = mutableListOf<MediaAsset>()
        val remoteMediaMap: Map<String, RemoteMedia> = remote.associateBy { it.checksum!! }

        val localAndRemoteHashes = HashSet<String>()

        local.forEach { localMedia ->
            val remoteMedia = remoteMediaMap[localMedia.checksum.toString()]
            if (remoteMedia != null) {
                mediaAssets.add(
                    LocalMedia(
                        remoteMedia.id,
                        localMedia.path,
                        localMedia.id,
                        localMedia.mimeType,
                        localMedia.checksum!!,
                        localMedia.timestamp
                    )
                )
                localAndRemoteHashes.add(localMedia.checksum!!)
            } else {
                mediaAssets.add(localMedia)
            }
        }

        remote.filterNot { it.checksum in localAndRemoteHashes }.forEach {
            mediaAssets.add(it)
        }

        mediaAssets.sortByDescending { it.timestamp }
        return mediaAssets
    }



}
