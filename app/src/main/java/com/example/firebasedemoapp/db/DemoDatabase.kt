package com.example.firebasedemoapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.firebasedemoapp.model.Item

@Database(
    entities = [Item::class],
    version = 1,
    exportSchema = false
)

abstract class DemoDatabase : RoomDatabase() {

    companion object {

        fun create(context: Context) : DemoDatabase {
            val databaseBuilder = Room.databaseBuilder(context, DemoDatabase::class.java, "firebasedemo.db")
            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun dao() : DemoDao
}