package com.example.firebasedemoapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.repository.NetworkState
import com.example.firebasedemoapp.repository.main.IMainRepository

class MainViewModel(private val repository: IMainRepository) : ViewModel() {

    /**
     * Items Vars
     */
    private val isItemsInitialized = MutableLiveData<Boolean>()
    private val itemsRepoResult = Transformations.map(isItemsInitialized) { repository.items() }
    val items = Transformations.switchMap(itemsRepoResult) { it.pagedList }
    val itemsNetworkState = Transformations.switchMap(itemsRepoResult) { it.networkState }


    fun initializeItems() {
        this.isItemsInitialized.value = true
    }

    var deleteItemsNetworkState = MutableLiveData<NetworkState>()
    fun deleteItems() {
        deleteItemsNetworkState = repository.deleteItems()
    }

    val addItemASFavoriteNetworkState = MutableLiveData<NetworkState>()
    fun addItemAsFavorite(item: Item) {
        addItemASFavoriteNetworkState.postValue(NetworkState.LOADING)

        repository.addItemAsFavoriteTask(item).addOnSuccessListener {
            addItemASFavoriteNetworkState.postValue(NetworkState.LOADED)
            repository.updateItemFavoriteStats(item.docId, true)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            addItemASFavoriteNetworkState.postValue(error)
        }
    }

    val removeItemAsFavoriteNetworkState = MutableLiveData<NetworkState>()
    fun removeItemAsFavorite(item: Item) {

        removeItemAsFavoriteNetworkState.postValue(NetworkState.LOADING)

        repository.removeItemASFavoriteTask(item).addOnSuccessListener {
            removeItemAsFavoriteNetworkState.postValue(NetworkState.LOADED)
            repository.updateItemFavoriteStats(item.docId, false)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            addItemASFavoriteNetworkState.postValue(error)
        }
    }

    /**
     * Auth
     */
    var deleteUserFavoritesNetworkState = MutableLiveData<NetworkState>()

    fun deleteAccount(uid: String?) {
        deleteUserFavoritesNetworkState = repository.deleteUser(uid)
    }
}