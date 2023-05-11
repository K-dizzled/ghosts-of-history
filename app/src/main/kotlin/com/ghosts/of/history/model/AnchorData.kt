package com.ghosts.of.history.model

data class GeoPosition(val latitude: Double, val longitude: Double)

typealias AnchorId = String

data class AnchorData(
    val anchorId: AnchorId,
    val name: String,
    val description: String?,
    val imageName: String?,
    val videoName: String,
    val scalingFactor: Float,
    val geoPosition: GeoPosition?,
    val isVisited: Boolean,
)