package com.example.firebasedemoapp.utils

import android.app.Application
import android.content.Context
import com.example.firebasedemoapp.db.DemoDatabase
import com.example.firebasedemoapp.repository.main.IMainRepository
import com.example.firebasedemoapp.repository.main.MainRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * Super simplified service locator implementation to allow us to replace default implementations
 * for testing.
 */
interface ServiceLocator {

    companion object {

        private val LOCK = Any()

        private var instance: ServiceLocator? = null

        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null)
                    instance = DefaultServiceLocator(
                        app = context.applicationContext as Application
                    )
                return instance!!
            }
        }
    }

    fun getMainActivityRepository() : IMainRepository

    fun getDiskIOExecutor(): Executor
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application) : ServiceLocator {

    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    private val db by lazy {
        DemoDatabase.create(app)
    }

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestoreSettings by lazy {
        FirebaseFirestoreSettings.Builder()
            .build()
    }

    private val firebaseFirestore by lazy {
        val firestoreReference = FirebaseFirestore.getInstance()
        firestoreReference.firestoreSettings = firestoreSettings
        firestoreReference
    }

    override fun getMainActivityRepository() : IMainRepository {
        return MainRepository(
            db = db,
            firestore = firebaseFirestore,
            firebaseAuth = firebaseAuth,
            ioExecutor = getDiskIOExecutor()
        )
    }

    override fun getDiskIOExecutor(): Executor = DISK_IO
}