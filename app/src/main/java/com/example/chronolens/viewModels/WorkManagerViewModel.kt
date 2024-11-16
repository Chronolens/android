package com.example.chronolens.viewModels

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

// TODO: clear notification here?
class WorkManagerViewModel(private val workManagerRepository: WorkManagerRepository) : ViewModel() {

    private val _workManagerState = MutableStateFlow(WorkManagerState())
    val workManagerState: StateFlow<WorkManagerState> = _workManagerState

    init {
        viewModelScope.launch {
            workManagerRepository.getPeriodicWorkInfo().collect { workInfoList ->
                // Assuming only one job, otherwise this breaks
                val workInfo = workInfoList.firstOrNull()
                _workManagerState.update { currState ->
                    currState.copy(
                        periodicWorkInfoState = workInfo?.state,
                        nextJob = workInfo?.nextScheduleTimeMillis,
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
        since: Long,
        includeVideos: Boolean,
        startNow: Boolean
    ) {
        viewModelScope.launch {
            workManagerRepository.periodicBackgroundSync(
                period = period,
                requireWifi = requireWifi,
                requireCharging = requireCharging,
                since = since,
                includeVideos = includeVideos,
                startNow = startNow
            )/*.collect { workInfoList ->
                // Assuming only one job, otherwise this breaks
                val workInfo = workInfoList.firstOrNull()
                _workManagerState.update { currState ->
                    currState.copy(
                        periodicWorkInfoState = workInfo?.state,
                        nextJob = workInfo?.nextScheduleTimeMillis,
                        isReady = !(workInfo?.state == WorkInfo.State.RUNNING || workInfo?.state == WorkInfo.State.ENQUEUED)
                    )
                }
            }*/
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