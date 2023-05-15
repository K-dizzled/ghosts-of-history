package com.ghosts.of.history.persistentcloudanchor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghosts.of.history.R
import com.ghosts.of.history.common.models.ItemAdapter
import com.ghosts.of.history.common.models.ItemModel
import com.ghosts.of.history.databinding.ActivityAnchorListBinding
import com.ghosts.of.history.utils.getAnchorsDataFromFirebase
import kotlinx.coroutines.*

class AnchorListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnchorListBinding
    private lateinit var job: Job
    private lateinit var uiScope: CoroutineScope

    val registerEditResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                uiScope.launch {
                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.adapter = ItemAdapter(fetchItems())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnchorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()
        uiScope = CoroutineScope(Dispatchers.Main + job)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        uiScope.launch {
            val items = fetchItems()
            recyclerView.adapter = ItemAdapter(items)
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = "Exhibit list"

    }

    private suspend fun fetchItems(): List<ItemModel> = withContext(Dispatchers.IO) {
        val anchorsData = getAnchorsDataFromFirebase()

        anchorsData.map { anchorData ->
            ItemModel(
                    anchorData,
                    uiScope,
                    applicationContext
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}