package com.example.spaceinvaders.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Result::class],
    exportSchema = false,
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ResultDAO
}


