package com.example.firebasedemoapp.view.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.utils.extentions.launchActivity
import com.example.firebasedemoapp.view.BaseActivity
import com.example.firebasedemoapp.view.main.MainActivity

class SplashScreenActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
    }

    override fun onUserLoggedIn() {
        Log.e("SplashScreenActivity", "launchActivity<MainActivity>")
        launchActivity<MainActivity> {
            putExtra(MainActivity.JUST_SIGNED_IN, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        finish()
    }

    override fun onUserLoggedOut() {

        mFirebaseAuth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("failed", task.exception.toString())
                }
            }
    }
}
