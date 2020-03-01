package com.example.firebasedemoapp.view.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.utils.Const.GOGGLE_SIGN_IN_INTENT
import com.example.firebasedemoapp.utils.extentions.launchActivity
import com.example.firebasedemoapp.view.main.MainActivity
import com.example.firebasedemoapp.view.splash.SplashScreenActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private val googleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    private val googleSignInClient by lazy {
        GoogleSignIn.getClient(this, googleSignInOptions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        initializeSignIn()
    }

    private fun initializeSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOGGLE_SIGN_IN_INTENT)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOGGLE_SIGN_IN_INTENT) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val googleSignInAccount = task.getResult(ApiException::class.java)
                if (googleSignInAccount != null) {

                    setUIToLoadingState()

                    getGoogleAuthCredential(googleSignInAccount)
                }
            } catch (e: ApiException) {
                Log.e("Firebase Auth", "onActivityResulty RC_SIGN_IN exception" + e.message)
            }
        }
    }

    private fun setUIToLoadingState() {
        textViewSigningUp.text = getString(R.string.loading_user)
        progressBarSigningIn.visibility = View.VISIBLE
    }

    private fun getGoogleAuthCredential(googleSignInAccount: GoogleSignInAccount) {
        val googleTokenId = googleSignInAccount.idToken
        val googleAuthCredential = GoogleAuthProvider.getCredential(googleTokenId, null)
        linkCurrentUserWithGoogleCredential(googleAuthCredential)
    }

    private fun linkCurrentUserWithGoogleCredential(googleAuthCredential: AuthCredential) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        currentUser?.linkWithCredential(googleAuthCredential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.e("AuthActivity", "Linked Google + Anon")
                launchActivity<MainActivity> {
                    putExtra(MainActivity.JUST_SIGNED_IN, true)
                }
                finish()
            } else {
                launchActivity<SplashScreenActivity> {  }
                finish()
            }
        }
    }
}
