package com.example.chronolens.viewModels

import androidx.lifecycle.ViewModel
import com.example.chronolens.repositories.WorkManagerRepository

// TODO: clear notification here?
class WorkManagerViewModel(private val workManagerRepository: WorkManagerRepository) : ViewModel() {

    fun periodicBackgroundSync(
        period: Long,
        requireWifi: Boolean,
        requireCharging: Boolean,
        since: Long,
        includeVideos: Boolean,
        startNow: Boolean
    ) {
        workManagerRepository.periodicBackgroundSync(
            period = period,
            requireWifi = requireWifi,
            requireCharging = requireCharging,
            since = since,
            includeVideos = includeVideos,
            startNow = startNow
        )
    }

    fun oneTimeBackgroundSync() {
        workManagerRepository.oneTimeBackgroundSync()
    }

    fun cancelPeriodicBackgroundSync() {
        workManagerRepository.cancelPeriodicBackgroundSync()
    }

    fun cancelOneTimeBackgroundSync() {
        workManagerRepository.cancelOneTimeBackgroundSync()
    }

}