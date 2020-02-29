package com.example.firebasedemoapp.repository.main

import androidx.paging.PagedList
import com.example.firebasedemoapp.androidx.PagingRequestHelper
import com.example.firebasedemoapp.model.Item
import com.example.firebasedemoapp.utils.Const.ITEMS_COLLECTION
import com.example.firebasedemoapp.utils.Const.NAME
import com.example.firebasedemoapp.utils.Const.OWNNER_ID
import com.example.firebasedemoapp.utils.createStatusLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

        val query = firestore.collection(ITEMS_COLLECTION)
            .whereEqualTo(OWNNER_ID, firebaseUser?.uid)
            .orderBy(NAME, Query.Direction.ASCENDING)
            .limit(pageSize)

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {

            try {
                query.get().addOnCompleteListener { task ->
                    val itemList = mutableListOf<Item>()

                    if (task.isSuccessful) {

                        val querySnapshot = task.result

                        for (document in querySnapshot!!) {
                            val item = document.toObject(Item::class.java)
                            itemList.add(item)
                        }

                        // Now get the last item from the list, this will be used as reference
                        // what to fetch next.
                        if (itemList.size < pageSize) {
                            isLastPageReached = true
                        } else {
                            lastVisibleSnapshot =
                                querySnapshot.documents[querySnapshot.size() - 1]
                        }

                        insertItemsIntoDb(itemList, it)
                    } else {

                        it.recordFailure(Throwable(task.exception?.message!!))
                    }
                }
            } catch (ioException: IOException) {
                it.recordFailure(Throwable(ioException.message ?: "unknown error"))
            }
        }
    }

    /**
     * User reached to the end of the list.
     */
    override fun onItemAtEndLoaded(itemAtEnd: Item) {

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {

            if (!isLastPageReached && lastVisibleSnapshot != null) {

                val nextQuery: Query = firestore.collection(ITEMS_COLLECTION)
                    .whereEqualTo(OWNNER_ID, firebaseUser?.uid)
                    .orderBy(NAME, Query.Direction.ASCENDING)
                    .startAfter(lastVisibleSnapshot!!)
                    .limit(pageSize)

                try {
                    nextQuery.get().addOnCompleteListener { task ->
                        val nextItemList = mutableListOf<Item>()
                        if (task.isSuccessful) {

                            val querySnapshot = task.result

                            for (document in querySnapshot!!) {
                                val item = document.toObject(Item::class.java)
                                nextItemList.add(item)
                            }

                            if (nextItemList.size < pageSize) {
                                isLastPageReached = true
                            } else {
                                lastVisibleSnapshot = querySnapshot.documents[querySnapshot.size() - 1]
                            }

                            insertItemsIntoDb(nextItemList, it)

                        } else {
                            it.recordFailure(Throwable(task.exception?.message!!))
                        }
                    }

                } catch (ioException: IOException) {
                    it.recordFailure(Throwable(ioException.message ?: "unknown error"))
                }
            } else {
                it.recordSuccess()
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