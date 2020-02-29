package com.example.firebasedemoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.firebasedemoapp.utils.ServiceLocator
import com.example.firebasedemoapp.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

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

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if (firebaseAuth.currentUser == null) {
            updateUI()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {

    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(this)
    }
}
