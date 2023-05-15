package com.ghosts.of.history.data

import kotlinx.coroutines.flow.StateFlow

import com.ghosts.of.history.model.*

data class AnchorsDataState(
    val anchorsData: Map<AnchorId, AnchorData> = emptyMap(),
    val visitedAnchorIds: Set<AnchorId> = setOf(),
)

interface AnchorsDataRepository {
    val state: StateFlow<AnchorsDataState>

    suspend fun load()
    suspend fun addVisited(anchorId: AnchorId)
    suspend fun removeVisited(anchorId: AnchorId)

    suspend fun saveAnchorData(
        anchorId: AnchorId,
        anchorData: AnchorData,
    )

    suspend fun updateAnchorData(anchorId: AnchorId, block: suspend (AnchorData) -> AnchorData)
}