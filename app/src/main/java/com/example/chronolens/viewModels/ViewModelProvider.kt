package com.example.chronolens.viewModels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.chronolens.ChronoLensApplication

object ViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MediaGridScreenViewModel(inventoryApplication().container.mediaGridRepository)
        }
    }
}

fun CreationExtras.inventoryApplication(): ChronoLensApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChronoLensApplication)