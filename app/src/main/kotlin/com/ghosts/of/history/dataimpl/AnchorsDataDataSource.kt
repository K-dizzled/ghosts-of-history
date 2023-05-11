package com.ghosts.of.history.dataimpl

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

import com.ghosts.of.history.model.*
import com.ghosts.of.history.utils.saveAnchorSetToFirebase

class AnchorsDataDataSource() {
    suspend fun save(anchorData: AnchorData) =
        saveAnchorSetToFirebase(anchorData)

    suspend fun load() =
        Firebase.firestore
            .collection("AnchorBindings")
            .whereNotEqualTo("video_name", "")
            .get()
            .await()
            .map {
                val latitude = it.get("latitude")
                val longitude = it.get("longitude")
                println(it.get("description"))
                AnchorData(
                    anchorId = it.get("id") as AnchorId,
                    name = it.get("name") as String,
                    description = it.get("description") as String?,
                    imageName = it.get("image_name") as String?,
                    videoName = it.get("video_name") as String,
                    scalingFactor = (it.get("scaling_factor") as Number).toFloat(),
                    geoPosition =
                    if (latitude != null && longitude != null) {
                        GeoPosition(
                            (latitude as Number).toDouble(),
                            (longitude as Number).toDouble()
                        )
                    } else {
                        null
                    },
                    isVisited = false
                )
            }
}