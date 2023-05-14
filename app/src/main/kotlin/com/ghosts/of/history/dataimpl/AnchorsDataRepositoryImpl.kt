package com.ghosts.of.history.dataimpl

import android.content.Context

import com.ghosts.of.history.data.AnchorsDataRepository
import com.ghosts.of.history.model.*
import com.ghosts.of.history.utils.saveAnchorSetToFirebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AnchorsDataRepositoryImpl private constructor(
    private val anchorsDataDataSource: AnchorsDataDataSource,
    private val visitedAnchorIdsDataSource: VisitedAnchorIdsDataSource,
) : AnchorsDataRepository {

    private val _anchorsData = MutableStateFlow<Map<AnchorId, AnchorData>>(emptyMap())

    override val anchorsData: StateFlow<Map<AnchorId, AnchorData>> = _anchorsData.asStateFlow()

    override suspend fun load() {
        val initialAnchorsData = anchorsDataDataSource.load()
        val initialVisitedAnchorIds = visitedAnchorIdsDataSource.load()

        _anchorsData.value =
            initialAnchorsData
                .map { it.copy(isVisited = initialVisitedAnchorIds.contains(it.anchorId)) }
                .associateBy { it.anchorId }
    }


    override suspend fun addVisited(anchorId: AnchorId) {
        updateAnchorData(anchorId) {
            it.copy(isVisited = true)
        }
    }

    override suspend fun removeVisited(anchorId: AnchorId) {
        updateAnchorData(anchorId) {
            it.copy(isVisited = false)
        }
    }

    override fun hasVisited(anchorId: AnchorId): Boolean {
        val anchorData = anchorsData.value[anchorId] ?: return false
        return anchorData.isVisited
    }

    override suspend fun updateAnchorData(anchorId: AnchorId, block: (AnchorData) -> AnchorData) {
        _anchorsData.update { anchorsData ->
            val anchorData = anchorsData[anchorId] ?: return@update anchorsData
            val newAnchorData = block(anchorData)
            val newAnchorsData = anchorsData.toMutableMap().apply {
                set(anchorId, newAnchorData)
            }.toMap()
            saveAnchorSetToFirebase(newAnchorData)
            visitedAnchorIdsDataSource.save(newAnchorsData.keys)
            anchorsData
        }
    }

    override suspend fun saveAnchorData(
        anchorId: AnchorId,
        anchorName: String,
        geoPosition: GeoPosition?
    ) {
        val anchor = AnchorData(
            anchorId = anchorId,
            name = anchorName,
            description = null,
            imageName = null,
            videoName = "",
            isEnabled = false,
            scalingFactor = 1.0f,
            geoPosition = geoPosition,
            isVisited = false
        )
        anchorsDataDataSource.save(anchor)
    }

    companion object {
        operator fun invoke(context: Context): AnchorsDataRepository =
            AnchorsDataRepositoryImpl(
                AnchorsDataDataSource(),
                VisitedAnchorIdsDataSource(context),
            )
    }
}