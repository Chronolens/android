package com.example.chronolens.repositories

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.chronolens.SyncManager
import com.example.chronolens.utils.Workmanager
import com.example.chronolens.workers.BackgroundChecksumWorker

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

    fun backgroundSync(){

//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.UNMETERED)    //Wifi
//            .setRequiredNetworkType(NetworkType.METERED)      //data
//            .setRequiresBatteryNotLow(true)
//            .build()

        var job = workManager.beginUniqueWork(
            Workmanager.BACKGROUND_UPLOAD_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.Companion.from(BackgroundChecksumWorker::class.java)
        )

        // Rest of job

        job.enqueue()

    }

    companion object {
        // A static reference for BackgroundChecksumWorker
        var syncManager: SyncManager? = null
            private set
    }

}