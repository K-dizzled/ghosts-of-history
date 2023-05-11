package com.ghosts.of.history.dataimpl

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

import com.ghosts.of.history.model.*
import com.ghosts.of.history.utils.*

class AnchorsDataDataSource() {
    suspend fun save(anchorData: AnchorData) =
        saveAnchorSetToFirebase(anchorData)

    suspend fun load() =
        getAnchorsDataFromFirebase()
}