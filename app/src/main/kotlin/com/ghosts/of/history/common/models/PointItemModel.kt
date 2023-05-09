package com.ghosts.of.history.common.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.ghosts.of.history.R
import androidx.recyclerview.widget.RecyclerView

data class ItemModel(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String
)

class ItemAdapter(private val itemList: List<ItemModel>) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.itemImageView)
        val titleView: TextView = view.findViewById(R.id.itemTitleView)
        val descriptionView: TextView = view.findViewById(R.id.itemDescriptionView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.titleView.text = item.name
        holder.descriptionView.text = item.description
        // holder.imageView.setImageResource(...)
    }

    override fun getItemCount() = itemList.size
}