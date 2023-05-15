package com.ghosts.of.history.utils

import android.content.Context
import com.ghosts.of.history.model.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.*


// onSuccessCallback processes an in-storage-path of this video
fun processVideoPathByName(videoName: String, onSuccessCallback: (String?) -> Unit) {
    Firebase.firestore.collection("StorageLinks").whereEqualTo("id", videoName).get()
        .addOnSuccessListener { docs ->
            val firstDoc = if (docs.documents.size > 0) {
                docs.documents[0]
            } else {
                null
            }
            onSuccessCallback(firstDoc?.get("in_storage_path") as String?)
        }
}

// onSuccessCallback processes an in-storage-path of this video
suspend fun saveAnchorToFirebase(
    anchorId: String,
    anchorName: String,
    latitude: Double?,
    longitude: Double?
) {
    val anchor = AnchorData(
            anchorId,
            anchorName,
            null,
            null,
            "",
            false,
            1.0f,
            if (latitude != null && longitude != null) {
                GeoPosition(latitude, longitude)
            } else {
                null
            }
    )
    saveAnchorSetToFirebase(anchor)
}

suspend fun saveAnchorSetToFirebase(anchor: AnchorData) {
    val videoParams = anchor.videoParams?.let {
        arrayOf(it.greenScreenColor.red,
                it.greenScreenColor.green,
                it.greenScreenColor.blue,
                it.chromakeyThreshold)
    }
    val document = mapOf(
            "id" to anchor.anchorId,
            "name" to anchor.name,
            "description" to anchor.description,
            "image_name" to anchor.imageName,
            "video_name" to anchor.videoName,
            "enabled" to anchor.isEnabled,
            "scaling_factor" to anchor.scalingFactor,
            "latitude" to anchor.geoPosition?.latitude,
            "longitude" to anchor.geoPosition?.longitude,
            "video_params" to videoParams,
    )
    Firebase.firestore.collection("AnchorBindings").document(anchor.anchorId).set(document).await()
}

suspend fun getAnchorsDataFromFirebase(): List<AnchorData> = Firebase.firestore.collection("AnchorBindings").whereNotEqualTo("video_name", "").get().await().map {
    val latitude = it.get("latitude")
    val longitude = it.get("longitude")
    val geoPosition = if (latitude != null && longitude != null) {
        GeoPosition((latitude as Number).toDouble(), (longitude as Number).toDouble())
    } else {
        null
    }
    val enabled = it.get("enabled") as Boolean? ?: false
    val videoParams = it.get("video_params")?. let { array ->
        val arr = array as ArrayList<*>
        VideoParams(
                Color(
                        (arr[0] as Number).toFloat(),
                        (arr[1] as Number).toFloat(),
                        (arr[2] as Number).toFloat()
                ),
                (arr[3] as Number).toFloat()
        )
    }
    println(it.get("description"))
    AnchorData(
            anchorId = it.get("id") as String,
            name = it.get("name") as String,
            description = it.get("description") as String?,
            imageName = it.get("image_name") as String?,
            videoName = it.get("video_name") as String,
            isEnabled = enabled,
            scalingFactor = (it.get("scaling_factor") as Number).toFloat(),
            geoPosition = geoPosition,
            videoParams = videoParams,
    )
}

suspend fun fetchVideoFromStorage(path: String, context: Context): Result<File> =
    fetchFileFromStorage("videos/$path", context)

suspend fun fetchImageFromStorage(path: String, context: Context): Result<File> =
    fetchFileFromStorage("images/$path", context)

suspend fun fetchFileFromStorage(path: String, context: Context): Result<File> = runCatching {
    println("FirestoreUtils: fetching $path with context $context")
    val url = URL(Firebase.storage.reference.child(path).downloadUrl.await().toString())
    withContext(Dispatchers.IO) {
        val connection = url.openConnection()
        connection.connect()
        val stream = connection.getInputStream()
        val randomFilename = UUID.randomUUID().toString() + File(path).name
        val downloadingMediaFile = File(context.cacheDir, randomFilename)

        val out = FileOutputStream(downloadingMediaFile)
        stream.copyTo(out)
        return@withContext downloadingMediaFile
    }
}

suspend fun fetchAllVideoNames(): Result<List<String>> = fetchAllFilenames("videos/")
suspend fun fetchAllImageNames(): Result<List<String>> = fetchAllFilenames("images/")

suspend fun fetchAllNamedImages(context: Context): Result<List<Pair<String, File>>> {
    val imageNamesRes = fetchAllImageNames().getOrElse { return Result.failure(it) }
    val imageFiles = imageNamesRes
        .map { fetchImageFromStorage(it, context) }
        .filter { it.isSuccess }
        .map { it.getOrThrow() }
    return Result.success(imageNamesRes.zip(imageFiles))
}

suspend fun fetchAllFilenames(path: String): Result<List<String>> = Result.runCatching {
    Firebase.storage.reference.child(path).listAll().await().items.map { it.name }
}



