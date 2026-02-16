package com.example.myquizbite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myquizbite.data.local.entity.TopicEntity

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY name")
    fun getAll(): List<TopicEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(topics: List<TopicEntity>)
}
