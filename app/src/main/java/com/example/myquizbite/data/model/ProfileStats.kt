package com.example.myquizbite.data.model

import com.example.myquizbite.data.local.entity.QuizResultEntity

data class QuizResultWithTitle(
    val result: QuizResultEntity,
    val quizTitle: String
)

data class ProfileStats(
    val correctRatio: Float,
    val totalAnswered: Int,
    val quizzesCompleted: Int,
    val recentResults: List<QuizResultWithTitle>
)
