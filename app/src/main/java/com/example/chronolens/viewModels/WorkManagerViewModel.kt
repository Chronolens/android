package com.example.chronolens.viewModels

import androidx.lifecycle.ViewModel
import com.example.chronolens.repositories.WorkManagerRepository

class WorkManagerViewModel(private val workManagerRepository: WorkManagerRepository) : ViewModel(){

    fun periodicBackgroundSync() {
        workManagerRepository.periodicBackgroundSync()
    }

    fun oneTimeBackgroundSync() {
        workManagerRepository.oneTimeBackgroundSync()
    }

    fun cancelPeriodicBackgroundSync(){
        workManagerRepository.cancelPeriodicBackgroundSync()
    }
    fun cancelOneTimeBackgroundSync(){
        workManagerRepository.cancelOneTimeBackgroundSync()
    }

}