/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ghosts.of.history.persistentcloudanchor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.ghosts.of.history.R
import com.ghosts.of.history.common.helpers.DisplayRotationHelper
import com.ghosts.of.history.model.AnchorData
import com.ghosts.of.history.model.GeoPosition
import com.ghosts.of.history.utils.saveAnchorSetToFirebase
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


/** Main Navigation Activity for the Persistent Cloud Anchor Sample.  */
class MainLobbyActivity : AppCompatActivity() {
    private lateinit var displayRotationHelper: DisplayRotationHelper
    private var selectedImage: Uri? = null
    private var selectedVideo: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_lobby)
        displayRotationHelper = DisplayRotationHelper(this)
        val hostButton = findViewById<MaterialButton>(R.id.host_button)
        hostButton.setOnClickListener { onHostButtonPress() }

        val btnPickImage = findViewById<Button>(R.id.button_image_select)
        btnPickImage.setOnClickListener {
            checkAndPickMedia(IMAGE_PICK_CODE)
        }

        val btnPickVideo = findViewById<Button>(R.id.button_video_select)
        btnPickVideo.setOnClickListener {
            checkAndPickMedia(VIDEO_PICK_CODE)
        }

        supportActionBar?.hide()
    }

    private fun checkAndPickMedia(pickCode: Int) {
        //check runtime permission
        if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) ==
                PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) ==
                PackageManager.PERMISSION_DENIED) {
            //permission denied
            val permissions = arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO);
            //show popup to request runtime permission
            requestPermissions(permissions, PERMISSION_CODE);
        }
        else{
            //permission already granted
            pickMediaFromGallery(pickCode);
        }
    }

    private fun pickMediaFromGallery(pickCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = if (pickCode == IMAGE_PICK_CODE) "image/*" else "video/*"
        startActivityForResult(intent, pickCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    pickMediaFromGallery(IMAGE_PICK_CODE)
                    pickMediaFromGallery(VIDEO_PICK_CODE)
                }
                else{
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_CODE -> {
                    val imageView = findViewById<ImageView>(R.id.image_view)
                    imageView.setImageURI(data?.data)
                    selectedImage = data?.data
                }
                VIDEO_PICK_CODE -> {
                    val videoUri = data?.data
                    selectedVideo = videoUri
                    val videoFileName = getFileName(videoUri)
                    val videoButton = findViewById<Button>(R.id.button_video_select)
                    videoButton.text = videoFileName
                }
                REQUEST_CODE_FROM_CHILD -> {
                    if (data == null) {
                        return
                    }

                    //val anchorName = data.getStringExtra("anchorName")!!
                    val anchorId = data.getStringExtra("anchorId")!!

                    val label = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.name_input_text).text
                    val description = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.description_input_text).text
                    //val image = File(selectedImage?.path!!)
                    //val video = File(selectedVideo?.path!!)

                    val geoposition = if (data.hasExtra("latitude") && data.hasExtra("longitude")) {
                        val latitude = data.getDoubleExtra("latitude", 0.0)
                        val longitude = data.getDoubleExtra("longitude", 0.0)

                        GeoPosition(latitude, longitude)
                    } else {
                        null
                    }

                    val anchorData = AnchorData(
                            anchorId,
                            label.toString(),
                            description.toString(),
                            null,
                            "",
                            true,
                            1.0f,
                            geoposition,
                            null,
                            false,
                    )

                    lifecycleScope.launch {
                        saveAnchorSetToFirebase(anchorData)
                    }
                }
            }
        }
    }

    private fun getFileName(fileUri: Uri?): String {
        var name = "unknown"
        val returnCursor = fileUri?.let { contentResolver.query(it, null, null, null, null) }
        returnCursor?.let { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            name = cursor.getString(nameIndex)
            cursor.close()
        }
        return name
    }

    override fun onResume() {
        super.onResume()
        displayRotationHelper.onResume()
    }

    public override fun onPause() {
        super.onPause()
        displayRotationHelper.onPause()
    }

    private fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? {
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(Environment.getExternalStorageDirectory().toString() + File.separator + fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }

    private fun onHostButtonPress() {
        val intent: Intent = CloudAnchorActivity.newHostingIntent(this)

        startActivityForResult(intent, REQUEST_CODE_FROM_CHILD)
    }

    /** Callback function invoked when the Resolve Button is pressed.  */
    private fun onResolveButtonPress() {
        val intent: Intent = CloudAnchorActivity.newResolvingIntent(this)
        startActivity(intent)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val VIDEO_PICK_CODE = 1001
        private const val PERMISSION_CODE = 1002
        private const val REQUEST_CODE_FROM_CHILD = 2228
    }
}