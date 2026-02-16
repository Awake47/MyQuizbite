package com.example.myquizbite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val quizId: String,
    val correctCount: Int,
    val totalCount: Int,
    val timeSpentSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
)
