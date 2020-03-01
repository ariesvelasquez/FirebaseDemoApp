package com.example.firebasedemoapp.view.main

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.repository.NetworkState

class ItemsRecyclerViewAdapter(
    private val onClickCallback: (v: View, position: Int, item: Item) -> Unit

) : PagedListAdapter<Item, RecyclerView.ViewHolder>(ITEM_COMPARATOR){

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            R.layout.recycler_view_layout_item -> ItemViewHolder.create(parent, onClickCallback)
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent, onClickCallback)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.recycler_view_layout_item -> (holder as ItemViewHolder).bind(getItem(position), position)
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bind(
                networkState)
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.network_state_item
        } else {
            R.layout.recycler_view_layout_item
        }
    }

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

    companion object {
        val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<Item>() {
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
                oldItem == newItem
        }
    }
}