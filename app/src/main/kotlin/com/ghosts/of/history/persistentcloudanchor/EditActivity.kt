package com.ghosts.of.history.persistentcloudanchor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ghosts.of.history.R
import com.ghosts.of.history.model.AnchorData
import com.ghosts.of.history.model.GeoPosition
import com.ghosts.of.history.utils.saveAnchorSetToFirebase
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class EditActivity : AppCompatActivity() {
    lateinit var anchorData: AnchorData
    private var editName: EditText? = null
    private var editDescription: EditText? = null
    private var editLatitude: EditText? = null
    private var editLongtitude: EditText? = null
    private var selectedImageUri : Uri? = null
    private var selectedVideoUri : Uri? = null

    private val viewModel: EditActivityViewModel by viewModels {
        EditActivityViewModel.Factory
    }

    val SELECT_IMAGE_REQUEST = 100
    val SELECT_VIDEO_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        editName = findViewById<EditText>(R.id.edit_name)
        editDescription = findViewById<EditText>(R.id.edit_description)
        editLatitude = findViewById<EditText>(R.id.edit_latitude)
        editLongtitude = findViewById<EditText>(R.id.edit_longtitude)

        val intent = intent
        val anchorDataString = intent.getStringExtra("anchorData")
        anchorData = Json.decodeFromString(AnchorData.serializer(), anchorDataString!!)

        editName?.setText(anchorData.name)
        editDescription?.setText(anchorData.description ?: "")
        editLatitude?.setText(anchorData.geoPosition?.latitude.toString() ?: "")
        editLongtitude?.setText(anchorData.geoPosition?.longitude.toString() ?: "")


        val buttonSelectImage = findViewById<Button>(R.id.button_select_image)
        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, SELECT_IMAGE_REQUEST)
        }

        val buttonSelectVideo = findViewById<Button>(R.id.button_select_video)
        buttonSelectVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, SELECT_VIDEO_REQUEST)
        }

        val buttonBack = findViewById<Button>(R.id.button_back)
        buttonBack.setOnClickListener { finish() }
        val buttonSave = findViewById<Button>(R.id.button_save)
        buttonSave.setOnClickListener { saveChanges() }
        val buttonDelete = findViewById<Button>(R.id.button_delete)
        buttonDelete.setOnClickListener { deleteAnchor() }
    }

    fun deleteAnchor() {}

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                SELECT_IMAGE_REQUEST -> {
                    selectedImageUri = data?.data
                }
                SELECT_VIDEO_REQUEST -> {
                    selectedVideoUri = data?.data
                }
            }
        }
    }

    private fun handleImage(): String? {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        selectedImageUri?.let { uri ->
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    return cursor.getString(nameIndex)
                }
            }
        }
        return null
    }

    private fun handleVideo(): String? {
        val projection = arrayOf(MediaStore.Video.Media.DISPLAY_NAME)
        selectedVideoUri?.let { uri ->
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                    return cursor.getString(nameIndex)
                }
            }
        }
        return null
    }

    private fun getGeoPosition(): GeoPosition? {
        val latitude: Double? = editLatitude?.text.toString().toDoubleOrNull()
        val longitude: Double? = editLongtitude?.text.toString().toDoubleOrNull()
        return if (latitude != null && longitude != null) {
            GeoPosition(latitude, longitude)
        } else {
            null
        }
    }

    private fun saveChanges() {
        val newAnchorData = AnchorData(
                anchorId = anchorData.anchorId,
                name = editName?.text.toString().let { it.ifEmpty { null } } ?: anchorData.name,
                description = editDescription?.text.toString().let { it.ifEmpty { null } } ?: anchorData.description,
                imageName = handleImage() ?: anchorData.imageName,
                videoName = handleVideo() ?: anchorData.videoName,
                isEnabled = anchorData.isEnabled,
                scalingFactor = anchorData.scalingFactor,
                geoPosition = getGeoPosition() ?: anchorData.geoPosition,
                videoParams = anchorData.videoParams
        )
        lifecycleScope.launch {
            viewModel.updateAnchorData(newAnchorData)
            finish()
        }
    }
}
