package com.ghosts.of.history.common.models

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.ghosts.of.history.R
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import androidx.lifecycle.lifecycleScope
import com.ghosts.of.history.utils.AnchorData
import com.ghosts.of.history.utils.fetchImageFromStorage
import com.ghosts.of.history.utils.saveAnchorSetToFirebase
import kotlinx.coroutines.launch

data class ItemModel(
    val anchorData: AnchorData,
    val scope: CoroutineScope,
    val context: Context
)

class ItemAdapter(private val itemList: List<ItemModel>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImageView)
        val titleView: TextView = view.findViewById(R.id.itemTitleView)
        val descriptionView: TextView = view.findViewById(R.id.itemDescriptionView)
        val checkBox: CheckBox = view.findViewById(R.id.enable_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.titleView.text = item.anchorData.name
        holder.descriptionView.text = item.anchorData.description ?: "No description"
        holder.checkBox.isChecked = item.anchorData.enabled
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            val changedAnchorData = item.anchorData.copy(enabled = isChecked)
            item.scope.launch {
                saveAnchorSetToFirebase(changedAnchorData)
            }
        }
        item.scope.launch {
            item.anchorData.imageName?.let {imgUrl ->
                val image = fetchImageFromStorage(imgUrl, item.context).getOrElse { return@let }
                val bitmap = BitmapFactory.decodeFile(image.absolutePath)
                holder.imageView.setImageBitmap(bitmap)
            }
        }
    }

    override fun getItemCount() = itemList.size
}