package com.example.myquizbite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress", primaryKeys = ["userId", "questionId"])
data class UserProgressEntity(
    val userId: String,
    val questionId: String,
    val correct: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
