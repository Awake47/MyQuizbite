package com.example.myquizbite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val iconName: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
