package com.example.chronolens.workers

import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.repositories.WorkManagerRepository
import com.example.chronolens.utils.showFinishedNotification
import com.example.chronolens.utils.showUploadNotification
import com.example.chronolens.utils.showSyncNotification
import com.example.chronolens.utils.updateSyncNotificationProgress
import com.example.chronolens.utils.updateUploadNotificationProgress
import kotlinx.coroutines.delay

private const val TAG = "UploadWorker"

class BackgroundChecksumWorker(ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        val syncManager = WorkManagerRepository.syncManager
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (syncManager != null) {

            // Sync Phase
            showSyncNotification(applicationContext)
//            delay(6000)
            val mediaGridRepository = syncManager.mediaGridRepository
            val localMedia: List<LocalMedia> = syncManager.getLocalAssets()
            val localMediaIds: List<String> = localMedia.map { it.id }
            val remoteAssets: Set<String> =
                syncManager.getRemoteAssets().map { it.checksum!! }.toSet()
            val checkSumsMap: Map<String, String> =
                mediaGridRepository.dbGetChecksumsFromList(localMediaIds)
                    .associate { it.localId to it.checksum }

            val mediaToUpload = mutableListOf<LocalMedia>()
            var calculated = 0

            updateSyncNotificationProgress(applicationContext, 0, localMedia.size)

            for (media in localMedia) {
//                delay(1000)
                val checksum = checkSumsMap[media.id]
                if (checksum != null) {
                    if (!remoteAssets.contains(checksum)) {
                        media.checksum = checksum
                        mediaToUpload.add(media)
                    }
                } else {
                    media.checksum =
                        mediaGridRepository.computeAndStoreChecksum(media.id, media.path)
                    if (!remoteAssets.contains(media.checksum)) {
                        mediaToUpload.add(media)
                    }
                }
                calculated++
                updateSyncNotificationProgress(applicationContext, calculated, localMedia.size)
            }

            notificationManager.cancel(1)

            // No media to upload
            if (mediaToUpload.isEmpty()) {
                return Result.success()
            }

            showUploadNotification(applicationContext, 0, mediaToUpload.size)

            // Upload Phase Logic
            var uploaded = 0
            mediaToUpload.forEach {
//                delay(2000)
                mediaGridRepository.apiUploadFileStream(it)
                uploaded++
                updateUploadNotificationProgress(applicationContext, uploaded, mediaToUpload.size)
            }

            notificationManager.cancel(2)
            showFinishedNotification(applicationContext,uploaded)

            return Result.success()
        } else {
            return Result.failure()
        }
    }
}
