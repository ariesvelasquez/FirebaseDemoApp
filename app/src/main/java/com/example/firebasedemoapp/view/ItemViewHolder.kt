package com.example.firebasedemoapp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.model.Item

class ItemViewHolder(
    val view: View,
    private val onClickCallback: (v: View, position: Int, item: Item) -> Unit
) : RecyclerView.ViewHolder(view) {

    fun bind(item: Item?, position: Int) {

//        val nameView =
        val imageViewFavorite = view.findViewById<ImageView>(R.id.imageViewFavorite)

        imageViewFavorite.setOnClickListener {
            onClickCallback.invoke(it, position, item!!)
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onClickCallback: (v: View, position: Int, item: Item) -> Unit
        ): ItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_view_layout_item, parent, false)
            return ItemViewHolder(view, onClickCallback)
        }
    }

}