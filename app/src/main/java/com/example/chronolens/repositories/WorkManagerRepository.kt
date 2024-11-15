package com.example.chronolens.repositories

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.chronolens.utils.SyncManager
import com.example.chronolens.utils.Work
import com.example.chronolens.workers.BackgroundChecksumWorker
import kotlinx.coroutines.flow.Flow
import java.time.Duration

// TODO: allow schedulling by hour of the day?
// TODO: settings check "from now on"?
class WorkManagerRepository(
    context: Context, mediaGridRepository: MediaGridRepository
) {

    private val workManager = WorkManager.getInstance(context)
    private val syncManager = SyncManager(mediaGridRepository)

    init {
        Companion.syncManager = syncManager
    }

    fun periodicBackgroundSync(
        period: Long,
        requireWifi: Boolean,
        requireCharging: Boolean,
        since: Long,
        includeVideos: Boolean,
        startNow: Boolean
    ): Flow<MutableList<WorkInfo>> {

        val networkType = if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
        val data = Data.Builder()
        data.putLong(Work.SINCE, since)

        val constraints = Constraints.Builder().setRequiredNetworkType(networkType)
            .setRequiresCharging(requireCharging).setRequiresBatteryNotLow(true).build()

        val job = PeriodicWorkRequestBuilder<BackgroundChecksumWorker>(
            repeatInterval = Duration.ofMinutes(period),
        ).setConstraints(constraints).setInputData(data.build())

//        if (startNow) {
//            job.setInitialDelay(Duration.ofSeconds(period))
//            .build()
//        } else {
//            job.build()
//        }

        // TODO: ExistingPeriodicWorkPolicy??
        workManager.enqueueUniquePeriodicWork(
            Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, job.build()
        )
        return workManager.getWorkInfosForUniqueWorkFlow(Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME)
    }

    fun cancelPeriodicBackgroundSync() {
        workManager.cancelUniqueWork(Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME)
    }

    // TODO: ExistingWorkPolicy??
    fun oneTimeBackgroundSync(): Flow<MutableList<WorkInfo>> {
        val job = OneTimeWorkRequestBuilder<BackgroundChecksumWorker>().build()

        workManager.enqueueUniqueWork(
            Work.ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME, ExistingWorkPolicy.KEEP, job
        )
        return workManager.getWorkInfosForUniqueWorkFlow(Work.ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME)
    }

    fun cancelOneTimeBackgroundSync() {
        workManager.cancelUniqueWork(Work.ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME)
    }

    companion object {
        var syncManager: SyncManager? = null
            private set
    }

}