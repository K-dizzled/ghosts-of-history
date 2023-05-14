package com.ghosts.of.history.model

data class GeoPosition(val latitude: Double, val longitude: Double)

typealias AnchorId = String

data class Color(val red: Float, val green: Float, val blue: Float)

data class VideoParams(val greenScreenColor: Color, val chromakeyThreshold: Float)

data class AnchorData(
    val anchorId: AnchorId,
    val name: String,
    val description: String?,
    val imageName: String?,
    val videoName: String,
    val isEnabled: Boolean,
    val scalingFactor: Float,
    val geoPosition: GeoPosition?,
    val videoParams: VideoParams? = null,
    val isVisited: Boolean = false,
)
