package com.example.firebasedemoapp.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.model.Item

class ItemViewHolder(
    val view: View,
    private val onClickCallback: (v: View, position: Int, item: Item) -> Unit
) : RecyclerView.ViewHolder(view) {

    fun bind(item: Item?, position: Int) {

        if (item == null) return

        // Init Views
        val nameView = view.findViewById<TextView>(R.id.textViewName)
        val imageViewFavorite = view.findViewById<ImageView>(R.id.imageViewFavorite)
        val progressFavorite = view.findViewById<ProgressBar>(R.id.progressFavorite)

        // Default UI State
        imageViewFavorite.visibility = View.VISIBLE
        progressFavorite.visibility = View.GONE


        // Set View Values
        nameView.text = item?.name

        // Handle Favorite State
        if (item?.isFavorite!!) {
            imageViewFavorite.setImageResource(R.drawable.ic_favorite)
        } else {
            imageViewFavorite.setImageResource(R.drawable.ic_not_favorite)
        }

        imageViewFavorite.setOnClickListener {
            it.visibility = View.GONE
            progressFavorite.visibility = View.VISIBLE
            onClickCallback.invoke(it, position, item)
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