package com.example.chronolens.repositories

import android.content.ContentResolver
import android.content.SharedPreferences
import android.util.Log
import com.example.chronolens.APIService
import com.example.chronolens.database.Checksum
import com.example.chronolens.database.ChecksumDao
import com.example.chronolens.database.RemoteAssetDao
import com.example.chronolens.database.RemoteAssetDb
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.ChecksumUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaGridRepository(
    private val checksumDao: ChecksumDao,
    private val remoteAssetDao: RemoteAssetDao,
    private val apiServiceClient: APIService,
    val sharedPreferences: SharedPreferences,
    val contentResolver: ContentResolver
) {

    suspend fun apiUploadFileStream(localMedia: LocalMedia) {
        val responseCode =
            apiServiceClient.uploadFileStream(sharedPreferences, localMedia)
        Log.i("LOG RESPONSECODE", responseCode.toString())
    }

    suspend fun apiSyncFullRemote(): List<RemoteMedia> {
        return apiServiceClient.syncFullRemote(sharedPreferences)
    }

    suspend fun apiSyncPartialRemote(lastSync: Long): Pair<List<RemoteMedia>, List<String>> {
        return apiServiceClient.syncPartialRemote(sharedPreferences, lastSync)
    }

    suspend fun apiGetPreview(id: String): String {
        return withContext(Dispatchers.IO) {
            apiServiceClient.getPreview(sharedPreferences, id)
        }
    }

    suspend fun apiGetFullImage(id: String): String {
        return withContext(Dispatchers.IO) {
            apiServiceClient.getFullImage(sharedPreferences, id)
        }
    }

    suspend fun dbStoreChecksumInDatabase(localId: String, checksum: String) {
        val newChecksum = Checksum(localId, checksum)
        checksumDao.insertChecksum(newChecksum)
    }

    suspend fun dbCheckChecksumInDatabase(localId: String): String? {
        return checksumDao.getChecksum(localId)?.checksum
    }

    suspend fun dbGetChecksumsFromList(ids: List<String>): List<Checksum> {
        val batchSize = 100
        val results = mutableListOf<Checksum>()
        ids.chunked(batchSize).forEach { chunk ->
            // Run the query for each chunk and add the results to the final list
            results += checksumDao.getChecksumsFromList(chunk)
        }
        return results
    }

    suspend fun dbUpsertRemoteAssets(remoteMedia: List<RemoteMedia>) {
        val remoteAssetsDb = remoteMedia.map {
            RemoteAssetDb(
                it.id, it.checksum!!,
                it.timestamp
            )
        }
        remoteAssetDao.upsertRemoteAssets(remoteAssetsDb)
    }

    suspend fun dbDeleteRemoteAssets(remoteIds: List<String>) {
        if (remoteIds.isNotEmpty()) {
            remoteAssetDao.deleteRemoteAssets(remoteIds)
        }
    }

    suspend fun dbGetRemoteAssets(): List<RemoteMedia> {
        return remoteAssetDao.getRemoteAssets().map {
            RemoteMedia(it.remoteId, it.checksum, it.timestamp)
        }
    }


    suspend fun getOrComputeChecksum(id: String, path: String): String {
        var checksum = checksumDao.getChecksum(id)?.checksum

        if (checksum == null) {
            checksum = ChecksumUtils().computeChecksum(path)
            val checksumDb = Checksum(id, checksum)
            checksumDao.insertChecksum(checksumDb)
        }
        return checksum
    }
}