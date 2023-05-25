package com.ghosts.of.history.persistentcloudanchor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ghosts.of.history.data.AnchorsDataRepository
import com.ghosts.of.history.model.AnchorData
import com.ghosts.of.history.model.AnchorId

class EditActivityViewModel constructor(
        private val anchorsDataRepository: AnchorsDataRepository
) : ViewModel() {
    suspend fun updateAnchorData(data: AnchorData) {
        anchorsDataRepository.updateAnchorData(data.anchorId) { data }
    }

    suspend fun removeAnchorData(anchorId: AnchorId) {
        anchorsDataRepository.removeAnchorData(anchorId)
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val anchorsDataRepository =
                        (this[APPLICATION_KEY] as App).anchorsDataRepository
                EditActivityViewModel(
                        anchorsDataRepository
                )
            }
        }
    }

}
