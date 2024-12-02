package com.example.chronolens.viewModels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chronolens.ChronoLensApplication

object ViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MediaGridViewModel(inventoryApplication().container.mediaGridRepository)
        }
        initializer {
            UserViewModel(inventoryApplication().container.userRepository)
        }
        initializer {
            WorkManagerViewModel(inventoryApplication().container.workManagerRepository)
        }
    }
}

fun CreationExtras.inventoryApplication(): ChronoLensApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChronoLensApplication)