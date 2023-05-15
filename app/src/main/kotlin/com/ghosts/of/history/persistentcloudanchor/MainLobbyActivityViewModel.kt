package com.ghosts.of.history.persistentcloudanchor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ghosts.of.history.data.AnchorsDataRepository
import com.ghosts.of.history.model.AnchorData

class MainLobbyActivityViewModel constructor(
        private val anchorsDataRepository: AnchorsDataRepository
) : ViewModel() {
    suspend fun saveAnchorData(data: AnchorData) {
        anchorsDataRepository.saveAnchorData(data.anchorId, data)
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val anchorsDataRepository =
                        (this[APPLICATION_KEY] as App).anchorsDataRepository
                MainLobbyActivityViewModel(
                        anchorsDataRepository
                )
            }
        }
    }

}
