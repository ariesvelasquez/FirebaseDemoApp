package com.example.firebasedemoapp.repository.main

import android.annotation.SuppressLint
import android.util.Log
import androidx.paging.toLiveData
import com.example.firebasedemoapp.db.DemoDatabase
import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.repository.Listing
import com.example.firebasedemoapp.utils.Const.COLLECTION_FAVORITES
import com.example.firebasedemoapp.utils.Const.DOC_ID
import com.example.firebasedemoapp.utils.Const.OWNER_ID
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    override fun addToFavorite(item: Item): Task<Void> {

        Log.e("repo addToFavorite : ", item.docId)

        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        // Add Favorit Ref
        val addToFavoriteId = item.docId + "_" + currentUser?.uid
        val addFavoriteRef = firestore.collection(COLLECTION_FAVORITES).document(addToFavoriteId)

        val favoriteMap = HashMap<String, Any?>()
        favoriteMap[DOC_ID] = addToFavoriteId
        favoriteMap[OWNER_ID] = currentUser?.uid

        return addFavoriteRef.set(favoriteMap)
    }
}

































