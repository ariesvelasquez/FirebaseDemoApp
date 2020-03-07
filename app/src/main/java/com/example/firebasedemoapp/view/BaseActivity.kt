package com.example.firebasedemoapp.view

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasedemoapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

abstract class BaseActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    protected var mFirebaseAuth = FirebaseAuth.getInstance()
    protected var mFirebaseUser = mFirebaseAuth.currentUser

    protected val mGoogleClient by lazy {
        GoogleSignIn.getClient(
            this, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(application.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )
    }

    abstract fun onUserLoggedOut()
    abstract fun onUserLoggedIn()

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {

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
