package com.example.myquizbite.data.model

data class Question(
    val id: String,
    val topicId: String,
    val text: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: Difficulty,
    val codeSnippet: String? = null
) {
    val isCorrect: (Int) -> Boolean = { selected -> selected == correctIndex }
}
