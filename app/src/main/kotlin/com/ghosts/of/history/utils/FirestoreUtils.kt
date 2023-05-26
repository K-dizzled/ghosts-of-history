package com.ghosts.of.history.utils

import android.content.Context
import android.net.Uri
import com.ghosts.of.history.model.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
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

private const val firebaseCollection = "AnchorBindings"

suspend fun saveAnchorSetToFirebase(anchor: AnchorData) {
    val videoParams = anchor.videoParams?.let {
        arrayOf(
            it.greenScreenColor.red,
            it.greenScreenColor.green,
            it.greenScreenColor.blue,
            it.chromakeyThreshold
        )
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
        "video_params" to videoParams?.toList(),
    )
    Firebase.firestore.collection(firebaseCollection).document(anchor.anchorId).set(document).await()
}

suspend fun removeAnchorDataFromFirebase(anchorId: AnchorId) =
    Firebase.firestore.collection(firebaseCollection)
        .document(anchorId).delete().await()

suspend fun getAnchorsDataFromFirebase(): List<AnchorData> =
    Firebase.firestore.collection(firebaseCollection).whereNotEqualTo("video_name", "").get().await()
        .map {
            val latitude = it.get("latitude")
            val longitude = it.get("longitude")
            val geoPosition = if (latitude != null && longitude != null) {
                GeoPosition((latitude as Number).toDouble(), (longitude as Number).toDouble())
            } else {
                null
            }
            val enabled = it.get("enabled") as Boolean? ?: false
            val videoParams = it.get("video_params")?.let { array ->
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

suspend fun getFileURL(path: String): String? =
    try {
        Firebase.storage.reference.child(path).downloadUrl.await().toString()
    } catch (e: StorageException) {
        null
    }

suspend fun fetchFileFromStorage(path: String, context: Context): Result<File> = runCatching {
    println("FirestoreUtils: fetching $path with context $context")
    val url = URL(getFileURL(path))
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

suspend fun uploadImageToStorage(uri: Uri) {
    Firebase.storage.reference.child("images/${uri.lastPathSegment}").putFile(uri).await()
}

suspend fun uploadVideoToStorage(uri: Uri) {
    Firebase.storage.reference.child("videos/${uri.lastPathSegment}").putFile(uri).await()
}

suspend fun uploadFileToStorage(path: String, uri: Uri) {
    Firebase.storage.reference.child(path).putFile(uri).await()
}

//suspend fun uploadFileToStorage(path: String, file: File): Result<Uri> = runCatching {
//    val storageRef = FirebaseStorage.getInstance().reference.child(path)
//    val uploadTask = storageRef.putFile(Uri.fromFile(file))
//    val uri = uploadTask.continueWithTask { task ->
//        if (!task.isSuccessful) {
//            task.exception?.let { throw it }
//        }
//        storageRef.downloadUrl
//    }.await()
//    uri
//}


