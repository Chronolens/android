package com.example.chronolens.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.chronolens.models.LocalMedia
import com.example.chronolens.models.RemoteMedia
import com.example.chronolens.repositories.WorkManagerRepository
import kotlinx.coroutines.delay

private const val TAG = "UploadWorker"

class BackgroundChecksumWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        val syncManager = WorkManagerRepository.syncManager
        Log.i("WORKER","WAITING 3 SECONDS")
        delay(5000)
        val localAssets: List<LocalMedia>? = syncManager?.getLocalAssets()
        Log.i("LOCALS_WORKER", localAssets.toString())
        val remoteAssets:List<RemoteMedia>? = syncManager?.getRemoteAssets()
        Log.i("REMOTES_WORKER", remoteAssets.toString())
        Log.i("WORKER", "FINISHED!")


        return Result.success()
    }
}