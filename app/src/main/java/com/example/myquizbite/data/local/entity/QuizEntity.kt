package com.example.myquizbite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myquizbite.data.model.Difficulty
import com.example.myquizbite.data.model.Quiz
import org.json.JSONArray

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val topicId: String,
    val title: String,
    val description: String,
    val difficulty: String,
    val questionIdsJson: String,
    val timePerQuestionSeconds: Int = 60,
    val isDailyChallenge: Boolean = false
) {
    fun toModel(): Quiz {
        val ids = mutableListOf<String>()
        if (questionIdsJson.isNotBlank()) {
            val arr = JSONArray(questionIdsJson)
            for (i in 0 until arr.length()) ids.add(arr.getString(i))
        }
        return Quiz(
            id = id,
            topicId = topicId,
            title = title,
            description = description,
            difficulty = Difficulty.valueOf(difficulty),
            questionIds = ids,
            timePerQuestionSeconds = timePerQuestionSeconds,
            isDailyChallenge = isDailyChallenge
        )
    }

    companion object {
        fun from(quiz: Quiz) = QuizEntity(
            id = quiz.id,
            topicId = quiz.topicId,
            title = quiz.title,
            description = quiz.description,
            difficulty = quiz.difficulty.name,
            questionIdsJson = JSONArray(quiz.questionIds).toString(),
            timePerQuestionSeconds = quiz.timePerQuestionSeconds,
            isDailyChallenge = quiz.isDailyChallenge
        )
    }
}
