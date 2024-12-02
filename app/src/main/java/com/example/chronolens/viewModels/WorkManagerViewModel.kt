package com.example.chronolens.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.chronolens.repositories.WorkManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkManagerState(
    val isLoading: Boolean = false,
    val isReady: Boolean = false,
    val nextJob: Long? = null,
    val oneTimeWorkInfoState: WorkInfo.State? = null,
    val periodicWorkInfoState: WorkInfo.State? = null
)

class WorkManagerViewModel(private val workManagerRepository: WorkManagerRepository) : ViewModel() {

    private val _workManagerState = MutableStateFlow(WorkManagerState())
    val workManagerState: StateFlow<WorkManagerState> = _workManagerState

    init {
        viewModelScope.launch {
            workManagerRepository.getPeriodicWorkInfo().collect { workInfoList ->
                // Assuming only one job, otherwise this breaks
                val workInfo = workInfoList.firstOrNull()
                _workManagerState.update { currState ->
                    Log.i("WORK", currState.periodicWorkInfoState.toString())
                    currState.copy(
                        periodicWorkInfoState = workInfo?.state,
                        nextJob = if (workInfo?.state == WorkInfo.State.ENQUEUED) workInfo.nextScheduleTimeMillis else null,
                        isReady = !(workInfo?.state == WorkInfo.State.RUNNING || workInfo?.state == WorkInfo.State.ENQUEUED)
                    )
                }
            }
        }
    }

    fun periodicBackgroundSync(
        period: Long,
        requireWifi: Boolean,
        requireCharging: Boolean,
        requireBatteryNotLow: Boolean,
        includeVideos: Boolean
    ) {
        viewModelScope.launch {
            workManagerRepository.periodicBackgroundSync(
                period = period,
                requireWifi = requireWifi,
                requireCharging = requireCharging,
                requireBatteryNotLow = requireBatteryNotLow,
                includeVideos = includeVideos
            )
        }
    }

    fun oneTimeBackgroundSync() {
        viewModelScope.launch {
            workManagerRepository.oneTimeBackgroundSync()
                .collect { workInfoList ->
                    val workInfo = workInfoList.firstOrNull()
                    _workManagerState.update { currState ->
                        currState.copy(
                            oneTimeWorkInfoState = workInfo?.state,
                            isLoading = (workInfo?.state == WorkInfo.State.RUNNING)
                        )
                    }
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
