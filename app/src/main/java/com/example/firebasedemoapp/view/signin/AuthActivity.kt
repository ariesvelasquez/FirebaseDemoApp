package com.example.firebasedemoapp.view.signin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebasedemoapp.R
import com.example.firebasedemoapp.utils.Const.GOGGLE_SIGN_IN_INTENT
import com.example.firebasedemoapp.utils.extentions.launchActivity
import com.example.firebasedemoapp.view.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_auth.*
import com.google.firebase.auth.FirebaseAuthUserCollisionException


class AuthActivity : AppCompatActivity() {

    companion object {
        const val FOR_RE_AUTH = "reauth"
        const val DELETED_ACCOUNT = "deletedAccount"
    }

    private var isForReAuthentication = false
    private var shouldResetGoogleOptions = false

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

        // Get Intent Extra
        isForReAuthentication = intent.getBooleanExtra(FOR_RE_AUTH, false)
        shouldResetGoogleOptions = intent.getBooleanExtra(DELETED_ACCOUNT, false)

        initializeSignIn()
    }

    private fun initializeSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOGGLE_SIGN_IN_INTENT)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        launchActivity<MainActivity> {  }
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
                Log.e("Firebase Auth", "getSignedInAccountFromIntent " + e.message)
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

        currentUser?.linkWithCredential(googleAuthCredential)?.addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                // Successful Link, redirect to MainActivity
                launchActivity<MainActivity> {
                    putExtra(MainActivity.JUST_SIGNED_IN, true)
                }
                finish()
            } else {
                // Handle Exception Errors
                try {
                    throw authTask.exception!!
                } catch (e: FirebaseAuthUserCollisionException) {
                    // Email already a linked, so just sign in.
                    signInUserWithGoogleCredential(googleAuthCredential)
                } catch (e: Exception) {
                    Toast.makeText(this, authTask.exception?.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun signInUserWithGoogleCredential(googleAuthCredential: AuthCredential) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.signInWithCredential(googleAuthCredential).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                // Check if the user is new
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    launchActivity<MainActivity> {
                        putExtra(MainActivity.FOR_DELETION_AFTER_RE_AUTH, isForReAuthentication)
                        putExtra(MainActivity.JUST_SIGNED_IN, true)
                    }
                    finish()
                }
            } else {
                Toast.makeText(this, authTask.exception?.message.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}
