package com.example.chronolens.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.repositories.WorkManagerRepository
import com.example.chronolens.utils.ChecksumUtils
import kotlinx.coroutines.delay

private const val TAG = "UploadWorker"

class BackgroundChecksumWorker(ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        val syncManager = WorkManagerRepository.syncManager
        if (syncManager != null) {
            Log.i("WORKER", "SLEEPING...")
            delay(5000)
            Log.i("WORKER", "STARTING CHECKSUMS")
            val mediaGridRepository = syncManager.mediaGridRepository

            val localMedia: List<LocalMedia> = syncManager.getLocalAssets()
            val localMediaIds: List<String> = localMedia.map { it.id }
            val remoteAssets: Set<String> =
                syncManager.getRemoteAssets().map { it.checksum!! }.toSet()
            val checkSumsMap: Map<String, String> =
                mediaGridRepository.dbGetChecksumsFromList(localMediaIds)
                    .associate { it.localId to it.checksum }

            var calculated = 0
            var notCalculated = 0

            val calculatedLocalMedia = mutableListOf<LocalMedia>()
            for (media in localMedia.filter { !remoteAssets.contains(it.checksum) }) {
                val checksum = checkSumsMap[media.id]
                if (checksum != null) {
                    // Already calculated
                    media.checksum = checksum
                    calculatedLocalMedia.add(media)
                    calculated++
                } else {
                    // To be calculated
                    media.checksum =
                        mediaGridRepository.computeAndStoreChecksum(media.id, media.path)
                    calculatedLocalMedia.add(media)
                    notCalculated++
                }
            }
            Log.i("WORKER", "Calculated:${calculated} | Not Calculated:${notCalculated}")

            Log.i("WORKER", "STARTING ${calculatedLocalMedia.size} UPLOADS")
            //In this list every media is guaranteed to have checksum
            calculatedLocalMedia.forEach {
                Log.i("WORKER", it.checksum.toString())
                mediaGridRepository.apiUploadFileStream(it)
            }
            Log.i("WORKER", "FINISHED ALL!")
            return Result.success()
        } else {
            return Result.failure()
        }
    }
}