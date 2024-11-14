package com.example.chronolens.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.chronolens.repositories.WorkManagerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// TODO: clear notification here?
class WorkManagerViewModel(private val workManagerRepository: WorkManagerRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _workInfoState = MutableStateFlow<WorkInfo.State?>(null)
    val workInfoState: StateFlow<WorkInfo.State?> = _workInfoState

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

        viewModelScope.launch {
            workManagerRepository.oneTimeBackgroundSync()
                .map { workInfoList ->
                    // Assuming only one job, otherwise this breaks
                    workInfoList.firstOrNull()?.state
                }
                .collect { state ->
                    _workInfoState.value = state
                    _isLoading.value = (state==WorkInfo.State.RUNNING)
                }
        }
    }

    fun cancelPeriodicBackgroundSync() {
        workManagerRepository.cancelPeriodicBackgroundSync()
    }

    fun cancelOneTimeBackgroundSync() {
        workManagerRepository.cancelOneTimeBackgroundSync()
    }

}