package com.example.firebasedemoapp.view.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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

    private var hasJustSignedIn = false
    private var isForDeletionAfterReAuth = false


    companion object {
        const val JUST_SIGNED_IN = "justSignedIn"
        const val FOR_DELETION_AFTER_RE_AUTH = "forDeletion"
        const val DELETE_ACCOUNT = "deleteAccount"
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

        // Get Intents
        isForDeletionAfterReAuth = intent.getBooleanExtra(FOR_DELETION_AFTER_RE_AUTH, false)

        // Display Signed User
        displaySignedInUser()

        initAdapter()

        initializeAndResetList()

        handleItemLiveData()

        handleDeleteAccountAfterReAuth()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_account -> initializeGoogleSignIn()
            R.id.action_delete_account -> deleteAccount()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun handleDeleteAccountAfterReAuth() {
        if (isForDeletionAfterReAuth) {
            deleteAccount()
        }
    }

    private fun deleteAccount() {
        val currentUID = mFirebaseUser?.uid
        // Remove Listener To Prevent method invokes
        mFirebaseAuth.removeAuthStateListener(this)
        mFirebaseUser?.delete()!!.addOnCompleteListener { deleteUserTask ->

            if (deleteUserTask.isSuccessful) {

                mFirebaseAuth.signOut()
                mGoogleClient.signOut()

                viewModel.deleteAccount(currentUID)
                viewModel.deleteUserFavoritesNetworkState.observe(this, Observer {
                    handleDeleteUserFavoritesNetworkState(it)
                })

            } else {
                val exceptionMessage = deleteUserTask.exception?.message.toString()
                Log.e("MainActivity", "DeleteUserTaskFailed: $exceptionMessage")
                // Add Listener To Prevent method invokes
                mFirebaseAuth.addAuthStateListener(this)
                reAuthUser()
            }
        }
    }

    private fun handleDeleteUserFavoritesNetworkState(it: NetworkState?) {
        when (it) {
            NetworkState.LOADING -> startLoading()
            NetworkState.LOADED -> updateUI(false)
            else -> {
                it?.msg?.let { recyclerViewItems.snack(it) {} }
                finishedLoading()
            }
        }
    }

    private fun reAuthUser() {
        mFirebaseAuth.removeAuthStateListener(this)
        mFirebaseAuth.signOut()

        launchActivity<AuthActivity> {
            putExtra(AuthActivity.FOR_RE_AUTH, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        finish()
    }

    private fun handleItemLiveData() {
        viewModel.items.observe(this, Observer {
            adapter.submitList(it)
        })

        viewModel.itemsNetworkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })

        viewModel.addItemASFavoriteNetworkState.observe(this, Observer {
            setProgressBarNetworkState(it, getString(R.string.added_item_as_favorite))
        })

        viewModel.removeItemAsFavoriteNetworkState.observe(this, Observer {
            setProgressBarNetworkState(it, getString(R.string.removed_item_as_favorite))
        })
    }

    private fun setProgressBarNetworkState(it: NetworkState?, message: String? = "") {
        when (it) {
            NetworkState.LOADING -> startLoading()
            NetworkState.LOADED -> {
                message?.let {
                    recyclerViewItems.snack(message) {}
                }
                finishedLoading()
            }
        }
    }

    /**
     * Check if the list data is needed to be refreshed.
     */
    private fun initializeAndResetList() {
        hasJustSignedIn = intent.getBooleanExtra(JUST_SIGNED_IN, false)
        val isConnectedToInternet = Tools().isNetworkAvailable(this)

        when {
            hasJustSignedIn && isConnectedToInternet -> {
                // Delete old list then re-fetch new items after successful deletion
                clearListItems()
            }
            else -> {
                // Just get the current items from room
                initializeListItems()
            }
        }
    }

    private fun initializeListItems() {
        viewModel.initializeItems()
    }

    private fun clearListItems() {
        viewModel.deleteItems()
        handleDeleteItemsNetworkState()
    }

    private fun handleDeleteItemsNetworkState() {
        viewModel.deleteItemsNetworkState.observe(this, Observer {
            when (it) {
                NetworkState.LOADING -> startLoading()
                NetworkState.LOADED -> {
                    recyclerViewItems.snack(getString(R.string.refreshed_item_list)) {}
                    finishedLoading()
                    initializeListItems()
                }
            }
        })
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

    private fun displaySignedInUser() {
        val userDisplay = if (mFirebaseUser?.isAnonymous!!) "Anonymous" else mFirebaseUser?.email
        Toast.makeText(this, "Account: $userDisplay", Toast.LENGTH_LONG).show()
    }

    private fun updateUI(isUserLoggedIn: Boolean) {
        if (isUserLoggedIn) {

        } else {
            // If not logged In, redirect to SplashScreenActivity to logged in as Anonymous
            launchActivity<SplashScreenActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            finish()
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

    private fun initializeGoogleSignIn() {
        launchActivity<AuthActivity> { }
        finish()
    }
}
