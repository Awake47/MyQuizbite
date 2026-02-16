package com.example.myquizbite.data.model

enum class UserRole { STUDENT, AUTHOR, ADMIN }

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole = UserRole.STUDENT,
    val level: Int = 1,
    val xp: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val quizzesCompleted: Int = 0
) {
    val levelProgress: Float
        get() = if (totalAnswers == 0) 0f else correctAnswers.toFloat() / totalAnswers
    val xpToNextLevel: Int get() = level * 100
}
