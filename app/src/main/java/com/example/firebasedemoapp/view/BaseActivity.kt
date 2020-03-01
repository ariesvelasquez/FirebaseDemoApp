package com.example.firebasedemoapp.view

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

abstract class BaseActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    protected var mFirebaseAuth = FirebaseAuth.getInstance()
    protected var mFirebaseUser = mFirebaseAuth.currentUser

    abstract fun onUserLoggedOut()
    abstract fun onUserLoggedIn()

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {

        Log.e("onAuthStateChanged", "onAuthStateChanged")

        mFirebaseAuth = firebaseAuth
        mFirebaseUser = firebaseAuth.currentUser

        if (mFirebaseUser == null) {
            onUserLoggedOut()
        } else {
            onUserLoggedIn()
        }
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        mFirebaseAuth.removeAuthStateListener(this)
    }
}