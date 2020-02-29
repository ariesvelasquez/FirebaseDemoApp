package com.example.firebasedemoapp.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.firebasedemoapp.model.Item

@Dao
interface DemoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(items: List<Item>)

    /*
     * Gets all items from items as DataSource
     */
    @Query("SELECT * FROM items")
    fun items(): DataSource.Factory<Int, Item>
}