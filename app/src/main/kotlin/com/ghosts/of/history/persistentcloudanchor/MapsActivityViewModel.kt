package com.ghosts.of.history.persistentcloudanchor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ghosts.of.history.data.AnchorsDataRepository

class MapsActivityViewModel constructor(
    anchorsDataRepository: AnchorsDataRepository
) : ViewModel() {
    val anchorsData = anchorsDataRepository.anchorsData

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val anchorsDataRepository =
                    (this[APPLICATION_KEY] as App).anchorsDataRepository
                MapsActivityViewModel(
                    anchorsDataRepository
                )
            }
        }
    }

}