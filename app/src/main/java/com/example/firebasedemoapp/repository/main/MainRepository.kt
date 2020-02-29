package com.example.firebasedemoapp.repository.main

import android.annotation.SuppressLint
import androidx.paging.toLiveData
import com.example.firebasedemoapp.db.DemoDatabase
import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.repository.Listing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executor

class MainRepository(
    val db: DemoDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val ioExecutor: Executor
) : IMainRepository {

    override fun items(): Listing<Item> {

        val pageSize = 4

        val boundaryCallback = ItemBoundaryCallback(
            pageSize = pageSize.toLong(),
            handleResponse = this::insertItemIntoDb,
            firestore = firestore,
            firebaseAuth = firebaseAuth,
            ioExecutor = ioExecutor
        )

        val pagedList = db.dao().items().toLiveData(
            pageSize = pageSize,
            boundaryCallback = boundaryCallback
        )

        return Listing(
            pagedList = pagedList,
            networkState = boundaryCallback.networkState
        )
    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    @SuppressLint("DefaultLocale")
    private fun insertItemIntoDb(list: List<Item>) {
        db.runInTransaction {

            db.dao().insertItems(list).run {

            }
        }
    }
}