package com.example.firebasedemoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item (
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var docId: String = "",
    var name: String = "",
    var order: Int = 0,
    var isFavorite: Boolean = false
)