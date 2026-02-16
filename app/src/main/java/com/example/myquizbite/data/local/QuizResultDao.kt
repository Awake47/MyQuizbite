package com.example.myquizbite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myquizbite.data.local.entity.QuizResultEntity

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getByUserId(userId: String): List<QuizResultEntity>

    @Query("SELECT COUNT(*) FROM quiz_results WHERE userId = :userId")
    fun getCountByUserId(userId: String): Int

    @Insert
    fun insert(result: QuizResultEntity)
}
