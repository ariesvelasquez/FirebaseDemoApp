package com.example.firebasedemoapp.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.utils.ServiceLocator
import com.example.firebasedemoapp.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private lateinit var adapter: ItemsRecyclerViewAdapter

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@MainActivity)
                    .getMainActivityRepository()
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initAdapter()

        handleItemLiveData()

        // Fetch Items
        viewModel.initializeItems()
    }

    private fun initAdapter() {

        adapter = ItemsRecyclerViewAdapter { view, pos, item ->

            when(view.id) {
                R.id.imageViewFavorite -> {
                    viewModel.addItemToFavorite(item)
                }
            }
        }

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        recyclerViewItems.layoutManager = linearLayoutManager
        recyclerViewItems.adapter = adapter
    }

    private fun handleItemLiveData() {

        viewModel.items.observe( this, Observer {
            adapter.submitList(it)
        })

        viewModel.itemsNetworkState.observe( this, Observer {
            adapter.setNetworkState(it)
        })
    }

    override fun onUserLoggedOut() {

    }

    override fun onUserLoggedIn(user: FirebaseUser) {

    }

    private fun updateUI() {

    }
}
