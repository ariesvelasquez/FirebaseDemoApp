package com.example.firebasedemoapp.view.main

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.repository.NetworkState
import com.example.firebasedemoapp.utils.ServiceLocator
import com.example.firebasedemoapp.utils.Tools
import com.example.firebasedemoapp.utils.extentions.launchActivity
import com.example.firebasedemoapp.utils.extentions.snack
import com.example.firebasedemoapp.view.BaseActivity
import com.example.firebasedemoapp.view.signin.AuthActivity
import com.example.firebasedemoapp.view.splash.SplashScreenActivity
import com.example.firebasedemoapp.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        const val JUST_SIGNED_IN = "justSignedIn"
    }

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

        checkIfNeedToResetList()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        val optionItem = menu?.findItem(R.id.action_delete_account)
        val accountItem = menu?.findItem(R.id.action_account)

        // Hide the overflow options if user is anonymous
        if (mFirebaseUser?.isAnonymous!!) {
            accountItem?.isVisible = true
            optionItem?.isVisible = false
        } else {
            accountItem?.isVisible = false
            optionItem?.isVisible = true
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_account -> initializeGoogleSignIn()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initAdapter() {

        adapter =
            ItemsRecyclerViewAdapter { view, pos, item ->

                when (view.id) {
                    R.id.imageViewFavorite -> handleFavoriteOnClick(item)
                }
            }

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        recyclerViewItems.layoutManager = linearLayoutManager
        recyclerViewItems.adapter = adapter
    }

    private fun handleItemLiveData() {

        viewModel.items.observe(this, Observer {
            adapter.submitList(it)
        })

        viewModel.itemsNetworkState.observe(this, Observer {
            Log.e("network state ", it.status.toString())
            adapter.setNetworkState(it)
        })

        viewModel.addItemASFavoriteNetworkState.observe(this, Observer {
            setProgressBarNetworkState(it, "Added as Favorite")
        })

        viewModel.removeItemAsFavoriteNetworkState.observe(this, Observer {
            setProgressBarNetworkState(it, "Removed as Favorite")
        })
    }

    private fun setProgressBarNetworkState(it: NetworkState?, message: String? = "") {
        when (it) {
            NetworkState.LOADING -> {
                startLoading()
            }
            NetworkState.LOADED -> {
                message?.let {
                    recyclerViewItems.snack(message) {}
                }
                finishedLoading()
            }
        }
    }

    private fun startLoading() {
        progressBarLoader.visibility = View.VISIBLE
    }

    private fun finishedLoading() {
        Handler().postDelayed({
            progressBarLoader.visibility = View.INVISIBLE
        }, 500)
    }

    /**
     * Check if the list data is needed to be refreshed.
     */
    private fun checkIfNeedToResetList() {
        val isJustSignedIn = intent.getBooleanExtra(JUST_SIGNED_IN, false)
        val hasInternet = Tools().isNetworkAvailable(this)

        if (isJustSignedIn || hasInternet) {
            viewModel.refreshItems()
        }
    }

    private fun handleFavoriteOnClick(item: Item) {
        if (!item.isFavorite) {
            viewModel.addItemAsFavorite(item)
        } else {
            viewModel.removeItemAsFavorite(item)
        }
    }

    override fun onUserLoggedOut() {
        updateUI(false)
    }

    override fun onUserLoggedIn() {
        updateUI(true)
    }

    private fun updateUI(isUserLoggedIn: Boolean) {
        if (isUserLoggedIn) {
            invalidateOptionsMenu()
        } else {
            launchActivity<SplashScreenActivity> { }
            finish()
        }
    }

    private fun initializeGoogleSignIn() {
        launchActivity<AuthActivity> {  }
    }
}
