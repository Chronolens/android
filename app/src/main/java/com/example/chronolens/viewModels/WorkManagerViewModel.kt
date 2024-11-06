package com.example.chronolens.viewModels

import androidx.lifecycle.ViewModel
import com.example.chronolens.repositories.WorkManagerRepository

class WorkManagerViewModel(private val workManagerRepository: WorkManagerRepository) : ViewModel(){

    fun backgroundSync() {
        workManagerRepository.backgroundSync()
    }


}