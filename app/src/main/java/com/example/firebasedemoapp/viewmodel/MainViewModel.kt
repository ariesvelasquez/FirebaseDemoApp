package com.example.firebasedemoapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
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
}