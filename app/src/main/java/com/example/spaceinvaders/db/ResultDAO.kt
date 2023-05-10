package com.example.spaceinvaders.db

import androidx.room.*

@Dao
interface ResultDAO {
    @Query("SELECT * FROM Result order by score DESC")
    fun getAll(): MutableList<Result>

    @Query("SELECT score FROM Result order by score DESC limit 1")
    fun getBest(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg items: Result)

    @Query("DELETE FROM Result")
    fun deleteAll()

}


