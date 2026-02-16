package com.example.myquizbite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myquizbite.data.local.entity.AchievementEntity

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY unlockedAt DESC")
    fun getByUser(userId: String): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievements WHERE userId = :userId AND id = :achievementId")
    fun hasAchievement(userId: String, achievementId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(achievement: AchievementEntity)
}
