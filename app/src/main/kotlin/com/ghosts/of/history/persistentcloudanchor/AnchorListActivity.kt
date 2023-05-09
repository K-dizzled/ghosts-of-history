package com.ghosts.of.history.persistentcloudanchor

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ghosts.of.history.R
import com.ghosts.of.history.common.models.ItemAdapter
import com.ghosts.of.history.common.models.ItemModel
import com.ghosts.of.history.databinding.ActivityAnchorListBinding
import kotlinx.coroutines.*

class AnchorListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnchorListBinding
    private lateinit var job: Job
    private lateinit var uiScope: CoroutineScope

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
        // Fetch items from network here.
        // This is just a placeholder.
        val items = ArrayList<ItemModel>()
        for (i in 1..100) {
            items.add(ItemModel("$i", "Name $i", "Description $i", "https://example.com/image/$i"))
        }
        return@withContext items
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}