package com.example.myquizbite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myquizbite.data.model.Question
import org.json.JSONArray
import com.example.myquizbite.data.model.Difficulty

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val text: String,
    val optionsJson: String,
    val correctIndex: Int,
    val explanation: String,
    val difficulty: String,
    val codeSnippet: String? = null
)

fun QuestionEntity.toModel(): Question {
    val options = mutableListOf<String>()
    if (optionsJson.isNotBlank()) {
        val arr = JSONArray(optionsJson)
        for (i in 0 until arr.length()) options.add(arr.getString(i))
    }
    return Question(
        id = id,
        topicId = topicId,
        text = text,
        options = options,
        correctIndex = correctIndex,
        explanation = explanation,
        difficulty = Difficulty.valueOf(difficulty),
        codeSnippet = codeSnippet
    )
}

fun Question.toEntity(): QuestionEntity = QuestionEntity(
    id = id,
    topicId = topicId,
    text = text,
    optionsJson = org.json.JSONArray(options).toString(),
    correctIndex = correctIndex,
    explanation = explanation,
    difficulty = difficulty.name,
    codeSnippet = codeSnippet
)
