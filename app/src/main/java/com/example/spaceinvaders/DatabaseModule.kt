package com.example.spaceinvaders

import android.content.Context
import androidx.room.Room
import com.example.spaceinvaders.db.AppDatabase
import com.example.spaceinvaders.db.ResultDAO
import dagger.Module
import dagger.Provides

@Module
class DatabaseModule(private val context: Context) {
    @Provides
    fun provideDatabase(): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "Result").build()
    }

    @Provides
    fun provideItemDao(database: AppDatabase): ResultDAO {
        return database.itemDao()
    }
}
