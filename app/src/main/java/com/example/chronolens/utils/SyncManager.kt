package com.example.chronolens.utils

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.chronolens.R
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.MediaAsset
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.repositories.MediaGridRepository
import java.io.File

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

    fun getAvailableAlbums(context: Context): List<String> {
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )

        val cursor = mediaGridRepository.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        )

        val albumList = mutableSetOf<String>()

        cursor?.use {
            val bucketNameColumn =
                it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (it.moveToNext()) {
                val albumName = it.getString(bucketNameColumn)?.takeIf { name ->
                    name.isNotBlank()
                } ?: "Unknown Album"

                File(it.getString(dataColumn)).parent ?: continue
                albumList.add(albumName)
            }
        }

        // App album is not displayed because it is supposed be selected anyways in order for download feature to work as intended
        albumList.remove(context.resources.getString(R.string.app_name))
        return albumList.toList()
    }


    fun getLocalAssets(albums: List<String>, context: Context): List<LocalMedia> {
        // At least app album will be loaded
        val albumsList = albums.toMutableList()
        albumsList.add(context.resources.getString(R.string.app_name))

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val selection = if (albumsList.isNotEmpty()) {
            albumsList.joinToString(
                prefix = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} IN (",
                postfix = ")",
                separator = ","
            ) { "?" }
        } else null
        val selectionArgs = albumsList.toTypedArray()

        val cursor = mediaGridRepository.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, null
        )

        val localMediaInfo = mutableListOf<LocalMedia>()

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateTakenColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateModifiedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val mimeTypeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(pathColumn)
                val dateTaken = if (dateTakenColumn != -1) it.getLong(dateTakenColumn) else null
                val dateAdded = if (dateAddedColumn != -1) it.getLong(dateAddedColumn) else null
                val mimeType = cursor.getString(mimeTypeColumn)

                val finalTimestamp: Long = when {
                    dateTaken != null && dateTaken > 0 -> {
                        dateTaken
                    }

                    dateAdded != null && dateAdded > 0 -> {
                        dateAdded * 1000
                    }

                    else -> {
                        it.getLong(dateModifiedColumn) * 1000
                    }
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
