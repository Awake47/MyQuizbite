package com.example.myquizbite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myquizbite.data.local.entity.UserProgressEntity

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND questionId = :questionId LIMIT 1")
    fun get(userId: String, questionId: String): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(progress: UserProgressEntity)

    @Query("SELECT AVG(CASE WHEN correct THEN 1.0 ELSE 0.0 END) FROM user_progress WHERE userId = :userId")
    fun getCorrectRatio(userId: String): Float?

    @Query("SELECT COUNT(*) FROM user_progress WHERE userId = :userId")
    fun getTotalAnswered(userId: String): Int

    @Query("SELECT COUNT(DISTINCT q.topicId) FROM user_progress up JOIN questions q ON up.questionId = q.id WHERE up.userId = :userId")
    fun getDistinctTopicsCount(userId: String): Int
}
