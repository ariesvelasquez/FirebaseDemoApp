package com.example.firebasedemoapp.repository.main

import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.repository.Listing
import com.google.android.gms.tasks.Task

interface IMainRepository {

    fun items() : Listing<Item>

    fun addItemAsFavoriteTask(item: Item): Task<Void>

    fun removeItemASFavoriteTask(item: Item): Task<Void>

    fun updateItemFavoriteStats(itemId: String, isFavorite: Boolean)

    fun refreshItems()
}