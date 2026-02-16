package com.example.myquizbite.data.model

data class Quiz(
    val id: String,
    val topicId: String,
    val title: String,
    val description: String,
    val difficulty: Difficulty,
    val questionIds: List<String>,
    val timePerQuestionSeconds: Int = 60,
    val isDailyChallenge: Boolean = false
)
