package com.ghosts.of.history.data

import kotlinx.coroutines.flow.StateFlow

import com.ghosts.of.history.model.*

interface AnchorsDataRepository {
    val anchorsData: StateFlow<Map<AnchorId, AnchorData>>

    suspend fun load()
    suspend fun addVisited(anchorId: AnchorId)
    suspend fun removeVisited(anchorId: AnchorId)
    fun hasVisited(anchorId: AnchorId): Boolean

    suspend fun saveAnchorData(
        anchorId: AnchorId,
        anchorName: String,
        geoPosition: GeoPosition?
    )

    suspend fun updateAnchorData(anchorId: AnchorId, block: (AnchorData) -> AnchorData)
}