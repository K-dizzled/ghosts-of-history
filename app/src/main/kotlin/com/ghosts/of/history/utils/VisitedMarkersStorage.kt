package com.ghosts.of.history.utils

import android.content.Context
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VisitedMarkersStorage private constructor(
    private val context: Context,
    markers: Set<String>
) {

    private var _markers: MutableSet<String> = markers.toMutableSet()

    suspend fun addMarker(marker: String) {
        _markers.add(marker)
        saveMarkers()
    }

    suspend fun removeMarker(marker: String) {
        _markers.remove(marker)
        saveMarkers()
    }

    fun hasMarker(marker: String): Boolean {
        return _markers.contains(marker)
    }

    private suspend fun saveMarkers() {
        val markersSnapshot = _markers.toSet()
        withContext(Dispatchers.IO) {
            val jsonString = Gson().toJson(markersSnapshot)
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(jsonString)
        }
    }

    companion object {
        private suspend fun loadMarkers(context: Context): Set<String>? =
            withContext(Dispatchers.IO) {
                val file = File(context.filesDir, FILE_NAME)
                if (file.exists()) {
                    val jsonString = file.readText()
                    val type = object : TypeToken<HashSet<String>>() {}.type
                    Gson().fromJson<Set<String>>(jsonString, type)
                } else {
                    null
                }
            }

        suspend operator fun invoke(context: Context): VisitedMarkersStorage =
            VisitedMarkersStorage(context, loadMarkers(context) ?: HashSet())

        private const val FILE_NAME = "markers.json"
    }
}
