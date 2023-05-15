package com.ghosts.of.history.persistentcloudanchor

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghosts.of.history.R
import com.ghosts.of.history.common.models.ItemAdapter
import com.ghosts.of.history.common.models.ItemModel
import com.ghosts.of.history.databinding.ActivityAnchorListBinding
import com.ghosts.of.history.model.AnchorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AnchorListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnchorListBinding
    private lateinit var job: Job
    private lateinit var uiScope: CoroutineScope
    private val viewModel: AnchorListActivityViewModel by viewModels {
        AnchorListActivityViewModel.Factory
    }

//    val registerEditResultLauncher: ActivityResultLauncher<Intent> =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                uiScope.launch {
//                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//                    recyclerView.adapter = ItemAdapter(fetchItems())
//                }
//            }
//        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAnchorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        job = Job()
        uiScope = CoroutineScope(Dispatchers.Main + job)

        val addMarkerButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.add_marker_button)
        addMarkerButton.setOnClickListener {
            startActivity(android.content.Intent(this, MainLobbyActivity::class.java))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.anchorsDataState
                            .collect {
                                val items: List<ItemModel> = it.anchorsData.map { entry ->
                                    mapAnchorDataToItemModel(entry.value)
                                }
                                recyclerView.adapter = ItemAdapter(items, viewModel)
                            }
                }
            }
        }

//        uiScope.launch {
//            val items = fetchItems()
//            recyclerView.adapter = ItemAdapter(items)
//        }

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = "Exhibit list"

    }

    private fun mapAnchorDataToItemModel(anchorData: AnchorData): ItemModel {
        return ItemModel(
                anchorData,
                uiScope,
                applicationContext
        )
    }

//    private suspend fun fetchItems(): List<ItemModel> = withContext(Dispatchers.IO) {
//        val anchorsData = getAnchorsDataFromFirebase()
//
//        anchorsData.map { anchorData ->
//            ItemModel(
//                    anchorData,
//                    uiScope,
//                    applicationContext
//            )
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}