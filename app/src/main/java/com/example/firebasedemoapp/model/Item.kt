package com.example.firebasedemoapp.model

import androidx.room.Entity

@Entity(tableName = "items")
data class Item (
    var docId: String = "",
    var name: String = "",
    var isFavorite: Boolean = false
)