package com.example.firebasedemoapp.view.splash

import android.os.Bundle
import android.util.Log
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.utils.extentions.launchActivity
import com.example.firebasedemoapp.view.BaseActivity
import com.example.firebasedemoapp.view.MainActivity
import com.google.firebase.auth.FirebaseUser

class SplashScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

    }

    override fun onUserLoggedIn(user: FirebaseUser) {
        launchActivity<MainActivity> {  }
    }

    override fun onUserLoggedOut() {
        mFirebaseAuth.signInAnonymously()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    Log.e("isSuccessful", "isSuccessful")
                } else {
                    Log.e("failed", task.exception.toString())
                }
            }
    }
}
