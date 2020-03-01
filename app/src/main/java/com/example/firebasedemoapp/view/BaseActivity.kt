package com.example.firebasedemoapp.view

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

abstract class BaseActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    protected val mFirebaseAuth = FirebaseAuth.getInstance()

    abstract fun onUserLoggedOut()
    abstract fun onUserLoggedIn(user: FirebaseUser)

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {

        Log.e("onAuthStateChanged", "onAuthStateChanged")


        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null) {
            onUserLoggedOut()
        } else {
            onUserLoggedIn(firebaseUser)
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