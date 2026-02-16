package com.example.myquizbite.navigation

object NavRoutes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val Profile = "profile"
    const val QuizList = "quiz_list"
    const val QuizRun = "quiz_run/{quizId}"
    const val Interview = "interview"
    const val InterviewRun = "interview_run/{mode}"
    const val Duel = "duel"
    const val DailyChallenge = "daily_challenge"

    fun quizRun(quizId: String) = "quiz_run/$quizId"
    fun interviewRun(mode: String) = "interview_run/$mode"
}
