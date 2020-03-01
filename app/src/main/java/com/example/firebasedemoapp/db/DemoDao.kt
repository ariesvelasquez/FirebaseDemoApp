package com.example.firebasedemoapp.db

import androidx.paging.DataSource
import androidx.room.*
import com.example.firebasedemoapp.model.Item

@Dao
interface DemoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(items: List<Item>)

    /*
     * Gets all items from items as DataSource
     */
    @Query("SELECT * FROM items ORDER BY `order`")
    fun items(): DataSource.Factory<Int, Item>

    @Query("UPDATE items SET isFavorite = :isFavorite WHERE docId = :itemDocId")
    fun updateItemFavorite(itemDocId: String, isFavorite: Boolean)

    @Query("DELETE FROM items")
    fun clearItems()
}