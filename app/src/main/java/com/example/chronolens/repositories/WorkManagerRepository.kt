package com.example.chronolens.repositories

import android.app.NotificationManager
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.chronolens.utils.Notification
import com.example.chronolens.utils.SyncManager
import com.example.chronolens.utils.Work
import com.example.chronolens.workers.BackgroundUploadWorker
import kotlinx.coroutines.flow.Flow
import java.time.Duration

class WorkManagerRepository(
    context: Context,
    mediaGridRepository: MediaGridRepository
) {

    private val workManager = WorkManager.getInstance(context)
    private val syncManager = SyncManager(mediaGridRepository)
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        Companion.syncManager = syncManager
    }

    fun periodicBackgroundSync(
        period: Long,
        requireWifi: Boolean,
        requireCharging: Boolean,
        requireBatteryNotLow: Boolean,
        includeVideos: Boolean
    ): Flow<MutableList<WorkInfo>> {

        val networkType = if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .setRequiresCharging(requireCharging)
            .setRequiresBatteryNotLow(requireBatteryNotLow)
            .build()

        val job = PeriodicWorkRequestBuilder<BackgroundUploadWorker>(
            repeatInterval = Duration.ofMinutes(period),
        ).setConstraints(constraints)

        workManager.enqueueUniquePeriodicWork(
            Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, job.build()
        )
        return workManager.getWorkInfosForUniqueWorkFlow(Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME)
    }

    fun cancelPeriodicBackgroundSync() {
        workManager.cancelUniqueWork(Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME)
        notificationManager.cancel(Notification.SYNC_CHANNEL_ID.ordinal)
        notificationManager.cancel(Notification.UPLOAD_CHANNEL_ID.ordinal)
        notificationManager.cancel(Notification.FINISHED_CHANNEL_ID.ordinal)
    }

    fun oneTimeBackgroundSync(): Flow<MutableList<WorkInfo>> {
        val job = OneTimeWorkRequestBuilder<BackgroundUploadWorker>().build()

        workManager.enqueueUniqueWork(
            Work.ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME, ExistingWorkPolicy.KEEP, job
        )
        return workManager.getWorkInfosForUniqueWorkFlow(Work.ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME)
    }

    fun cancelOneTimeBackgroundSync() {
        workManager.cancelUniqueWork(Work.ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME)
        notificationManager.cancel(Notification.SYNC_CHANNEL_ID.ordinal)
        notificationManager.cancel(Notification.UPLOAD_CHANNEL_ID.ordinal)
        notificationManager.cancel(Notification.FINISHED_CHANNEL_ID.ordinal)
    }

    fun getPeriodicWorkInfo(): Flow<MutableList<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkFlow(Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME)
    }

    companion object {
        var syncManager: SyncManager? = null
            private set
    }

}