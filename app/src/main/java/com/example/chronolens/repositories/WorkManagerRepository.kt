package com.example.chronolens.repositories

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.chronolens.utils.SyncManager
import com.example.chronolens.utils.Work
import com.example.chronolens.workers.BackgroundChecksumWorker
import java.time.Duration

// TODO: allow schedulling by hour of the day?
// TODO: settings check "from now on"?
class WorkManagerRepository(
    context: Context,
    mediaGridRepository: MediaGridRepository
) {

    private val workManager = WorkManager.getInstance(context)
    private val syncManager = SyncManager(mediaGridRepository)

    init {
        // Set a static reference to mediaGridRepository
        Companion.syncManager = syncManager
    }

    // TODO: make it dynamic with user prefs
    // TODO: set initial delay so that it doesn't execute right away
    fun periodicBackgroundSync() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)    //Wifi
            //.setRequiredNetworkType(NetworkType.METERED)    //Data
            .setRequiresBatteryNotLow(true)
            .build()


        val job = PeriodicWorkRequestBuilder<BackgroundChecksumWorker>(
            repeatInterval = Duration.ofMinutes(15),
        ).setConstraints(constraints)
            .setInitialDelay(Duration.ofSeconds(5))
            .build()

        workManager.enqueueUniquePeriodicWork(
            Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            job
        )

    }

    fun cancelPeriodicBackgroundSync() {
        workManager.cancelUniqueWork(Work.PERIODIC_BACKGROUND_UPLOAD_WORK_NAME)
    }

    // TODO: without constraints?
    fun oneTimeBackgroundSync() {

        val job = OneTimeWorkRequestBuilder<BackgroundChecksumWorker>().build()

        workManager.enqueueUniqueWork(
            Work.ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            job
        )

    }

    fun cancelOneTimeBackgroundSync() {
        workManager.cancelUniqueWork(Work.ONE_TIME_BACKGROUND_UPLOAD_WORK_NAME)
    }

    companion object {
        // A static reference for BackgroundChecksumWorker
        var syncManager: SyncManager? = null
            private set
    }

}