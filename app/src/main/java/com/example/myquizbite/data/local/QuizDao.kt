package com.example.myquizbite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myquizbite.data.local.entity.QuizEntity

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY title")
    fun getAll(): List<QuizEntity>

    @Query("SELECT * FROM quizzes WHERE topicId = :topicId")
    fun getByTopicId(topicId: String): List<QuizEntity>

    @Query("SELECT * FROM quizzes WHERE id = :id")
    fun getById(id: String): QuizEntity?

    @Query("SELECT * FROM quizzes WHERE isDailyChallenge = 1 LIMIT 1")
    fun getDailyChallenge(): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(quizzes: List<QuizEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(quiz: QuizEntity)

    @Query("DELETE FROM quizzes WHERE id = :id")
    fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM quizzes")
    fun getCount(): Int
}
