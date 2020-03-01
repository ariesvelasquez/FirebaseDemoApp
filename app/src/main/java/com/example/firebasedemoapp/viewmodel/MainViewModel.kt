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

    val addToFavoritNetworkState = MutableLiveData<NetworkState>()

    fun addItemToFavorite(item: Item) {
        addToFavoritNetworkState.postValue(NetworkState.LOADING)

        repository.addToFavorite(item).addOnSuccessListener {
            addToFavoritNetworkState.postValue(NetworkState.LOADED)
        }.addOnFailureListener {
            val error = NetworkState.error(it.message)
            addToFavoritNetworkState.postValue(error)
        }
    }
}