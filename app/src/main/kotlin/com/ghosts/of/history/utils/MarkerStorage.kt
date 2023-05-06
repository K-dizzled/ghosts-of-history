package com.ghosts.of.history.utils

import android.content.Context
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.File
import java.io.IOException

class MarkerStorage(private val context: Context) {
    private val gson = Gson()
    private var markers: HashSet<String>? = null

    init {
        if (!loadMarkers()) {
            markers = HashSet()
        }
    }

    fun addMarker(marker: String) {
        markers?.add(marker)
        saveMarkers()
    }

    fun removeMarker(marker: String) {
        markers?.remove(marker)
        saveMarkers()
    }

    fun hasMarker(marker: String): Boolean {
        return markers?.contains(marker) ?: false
    }

    private fun saveMarkers() {
        val jsonString = gson.toJson(markers)
        val file = File(context.filesDir, FILE_NAME)

        try {
            file.writeText(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadMarkers(): Boolean {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) {
            try {
                val jsonString = file.readText()
                val type = object : TypeToken<HashSet<String>>() {}.type
                markers = gson.fromJson(jsonString, type)
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    companion object {
        private const val FILE_NAME = "markers.json"
    }
}
