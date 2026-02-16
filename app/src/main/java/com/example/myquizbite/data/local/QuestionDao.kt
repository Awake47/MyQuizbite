package com.example.myquizbite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myquizbite.data.local.entity.QuestionEntity

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    fun getByIds(ids: List<String>): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE topicId = :topicId")
    fun getByTopicId(topicId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions")
    fun getAll(): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(questions: List<QuestionEntity>)
}
