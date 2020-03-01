package com.example.firebasedemoapp.view

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.repository.NetworkState

class ItemsRecyclerViewAdapter(
    private val onClickCallback: (v: View, position: Int, item: Item) -> Unit

) : PagedListAdapter<Item, RecyclerView.ViewHolder>(ITEM_COMPARATOR){

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.create(parent, onClickCallback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        return (holder as ItemViewHolder).bind(item, position)
    }

    companion object {
        val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<Item>() {
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem == newItem
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

}