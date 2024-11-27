package com.example.chronolens.repositories

import android.content.ContentResolver
import android.content.SharedPreferences
import com.example.chronolens.utils.APIUtils
import com.example.chronolens.database.Checksum
import com.example.chronolens.database.ChecksumDao
import com.example.chronolens.database.RemoteAssetDao
import com.example.chronolens.database.RemoteAssetDb
import com.example.chronolens.models.FullMedia
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.Person
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.utils.ChecksumUtils
import com.example.chronolens.utils.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaGridRepository(
    private val checksumDao: ChecksumDao,
    private val remoteAssetDao: RemoteAssetDao,
    val sharedPreferences: SharedPreferences,
    val contentResolver: ContentResolver
) {

    suspend fun uploadMedia(
        localMedia: List<LocalMedia>,
        setProgress: (Int) -> Unit = {}
    ): List<Pair<String?, String>> {
        return APIUtils.uploadMedia(sharedPreferences, localMedia, setProgress)
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

    suspend fun apiGetFullImage(id: String): FullMedia? {
        return withContext(Dispatchers.IO) {
            APIUtils.getFullImage(sharedPreferences, id)
        }
    }

    suspend fun apiGetPeople(): List<Person> {
        return APIUtils.getPeople(sharedPreferences)
    }


    suspend fun dbGetChecksumsFromList(ids: List<Long>): List<Checksum> {
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

    suspend fun getOrComputeChecksum(id: Long, path: String): String {
        var checksum = checksumDao.getChecksum(id)?.checksum

        if (checksum == null) {
            checksum = ChecksumUtils().computeChecksum(path)
            val checksumDb = Checksum(id, checksum)
            checksumDao.insertChecksum(checksumDb)
        }
        return checksum
    }

    suspend fun computeAndStoreChecksum(id: Long, path: String): String {
        val checksum = ChecksumUtils().computeChecksum(path)
        val checksumDb = Checksum(id, checksum)
        checksumDao.insertChecksum(checksumDb)
        return checksum
    }

    suspend fun apiGetClusterPreviewsPage(
        clusterId: Int,
        page: Int,
        pageSize: Int,
        requestType: String
    ): List<Pair<String, String>>? {
        return APIUtils.getClusterPreviewsPage(
            sharedPreferences,
            clusterId,
            page,
            pageSize,
            requestType
        )
    }

    suspend fun apiGetNextClipSearchPage(
        search: String,
        page: Int,
        pageSize: Int
    ): List<Pair<String, String>>? {
        return APIUtils.loadNextClipSearchPage(sharedPreferences, search, page, pageSize)
    }

    fun getUserAlbums(): List<String>? {
        val albums = sharedPreferences.getStringSet(Prefs.ALBUMS, null)
        return albums?.toList()
    }
}