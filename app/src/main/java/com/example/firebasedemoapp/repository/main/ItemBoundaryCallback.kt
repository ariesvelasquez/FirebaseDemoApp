package com.example.firebasedemoapp.repository.main

import androidx.paging.PagedList
import com.example.firebasedemoapp.androidx.PagingRequestHelper
import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.utils.Const
import com.example.firebasedemoapp.utils.Const.COLLECTION_ITEMS
import com.example.firebasedemoapp.utils.Const.NAME
import com.example.firebasedemoapp.utils.createStatusLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.io.IOException
import java.util.concurrent.Executor

class ItemBoundaryCallback(
    private val pageSize: Long,
    private val handleResponse: (list: List<Item>) -> Unit,
    private val firestore: FirebaseFirestore,
    firebaseAuth: FirebaseAuth,
    private val ioExecutor: Executor

) : PagedList.BoundaryCallback<Item>() {

    private var lastVisibleSnapshot: DocumentSnapshot? = null
    private var isLastPageReached: Boolean = false

    private var firebaseUser: FirebaseUser? = firebaseAuth.currentUser

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    override fun onZeroItemsLoaded() {

        val query = firestore.collection(COLLECTION_ITEMS)
            .orderBy(NAME, Query.Direction.ASCENDING)
            .limit(pageSize)

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) { helper ->

            try {
                callSetOfItems(query, helper)
            } catch (ioException: IOException) {
                helper.recordFailure(Throwable(ioException.message ?: "unknown error"))
            }
        }
    }

    /**
     * User reached to the end of the list.
     */
    override fun onItemAtEndLoaded(itemAtEnd: Item) {

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {

            if (!isLastPageReached && lastVisibleSnapshot != null) {

                val nextQuery: Query = firestore.collection(COLLECTION_ITEMS)
                    .orderBy(NAME, Query.Direction.ASCENDING)
                    .startAfter(lastVisibleSnapshot!!)
                    .limit(pageSize)

                try {
                    callSetOfItems(nextQuery, it)

                } catch (ioException: IOException) {
                    it.recordFailure(Throwable(ioException.message ?: "unknown error"))
                }
            } else {
                it.recordSuccess()
            }
        }
    }

    /**
     * Fetch batch of items from a firestore collection then save it to db.
     */
    private fun callSetOfItems(query: Query, helper: PagingRequestHelper.Request.Callback) {
        query.get().addOnCompleteListener { task ->
            val itemList = mutableListOf<Item>()
            val favoriteRefList = mutableListOf<DocumentReference>()

            if (task.isSuccessful) {

                val querySnapshotResult = task.result

                for (document in querySnapshotResult!!) {
                    val item = document.toObject(Item::class.java)

                    // Set the possible favorite id from item docId and user uid
                    val possibleFavoriteId = item.docId + "_" + firebaseUser?.uid.toString()

                    // Define favorite ref then add it to the list of reference
                    // that will be checked later
                    val favoriteRef = firestore.collection(Const.COLLECTION_FAVORITES)
                        .document(possibleFavoriteId)

                    favoriteRefList.add(favoriteRef)

                    // Also add the fetched item.
                    itemList.add(item)
                }

                // Check all the collected favorite ref if exist
                firestore.runTransaction { transaction ->

                    itemList.forEachIndexed { index, item ->
                        val isFavorite = transaction.get(favoriteRefList[index])

                        if (isFavorite.exists()) {
                            itemList[index].isFavorite = true
                        }
                    }

                }.addOnSuccessListener {

                    // Now get the last item from the list, this will be used as reference
                    // what to fetch next.
                    if (itemList.size < pageSize) {
                        isLastPageReached = true
                    } else {
                        lastVisibleSnapshot =
                            querySnapshotResult.documents[querySnapshotResult.size() - 1]
                    }

                    insertItemsIntoDb(itemList, helper)

                }.addOnFailureListener { exception ->
                    helper.recordFailure(Throwable(exception.message))
                }

            } else {
                helper.recordFailure(Throwable(task.exception?.message!!))
            }
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
        list: List<Item>,
        it: PagingRequestHelper.Request.Callback
    ) {
        ioExecutor.execute {
            handleResponse(list)
            it.recordSuccess()
        }
    }
}