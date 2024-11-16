package com.example.chronolens.repositories

import android.content.ContentResolver
import android.content.SharedPreferences
import com.example.chronolens.utils.APIUtils
import com.example.chronolens.database.Checksum
import com.example.chronolens.database.ChecksumDao
import com.example.chronolens.database.RemoteAssetDao
import com.example.chronolens.database.RemoteAssetDb
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.Person
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.ChecksumUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaGridRepository(
    private val checksumDao: ChecksumDao,
    private val remoteAssetDao: RemoteAssetDao,
    val sharedPreferences: SharedPreferences,
    val contentResolver: ContentResolver
) {

    suspend fun apiUploadFileStream(localMedia: LocalMedia) :String? {
        return APIUtils.uploadMedia(sharedPreferences, localMedia)
    }

    suspend fun apiSyncFullRemote(): List<RemoteMedia> {
        return APIUtils.syncFullRemote(sharedPreferences)
    }

    suspend fun apiSyncPartialRemote(lastSync: Long): Pair<List<RemoteMedia>, List<String>> {
        return APIUtils.syncPartialRemote(sharedPreferences, lastSync)
    }

    suspend fun apiGetPreview(id: String): String {
        return withContext(Dispatchers.IO) {
            APIUtils.getPreview(sharedPreferences, id)
        }
    }

    suspend fun apiGetFullImage(id: String): String {
        return withContext(Dispatchers.IO) {
            APIUtils.getFullImage(sharedPreferences, id)
        }
    }

    suspend fun apiGetPeople(): List<Person> {
        return APIUtils.getPeople(sharedPreferences)
    }

    suspend fun dbStoreChecksumInDatabase(localId: String, checksum: String) {
        val newChecksum = Checksum(localId, checksum)
        checksumDao.insertChecksum(newChecksum)
    }

    suspend fun dbCheckChecksumInDatabase(localId: String): String? {
        return checksumDao.getChecksum(localId)?.checksum
    }

    suspend fun dbGetChecksumsFromList(ids: List<String>): List<Checksum> {
        val batchSize = 500
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

    suspend fun computeAndStoreChecksum(id: String, path: String): String {
        val checksum = ChecksumUtils().computeChecksum(path)
        val checksumDb = Checksum(id, checksum)
        checksumDao.insertChecksum(checksumDb)
        return checksum
    }

    fun apiGetPersonPhotos(faceId: Int, type: String): List<RemoteMedia> {
        return APIUtils.getPersonPhotos(sharedPreferences, faceId, type)
    }
}