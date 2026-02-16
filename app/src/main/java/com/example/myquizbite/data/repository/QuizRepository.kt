package com.example.myquizbite.data.repository

import com.example.myquizbite.data.local.AppDatabase
import com.example.myquizbite.data.local.SeedData
import com.example.myquizbite.data.local.entity.QuizResultEntity
import com.example.myquizbite.data.local.entity.UserProgressEntity
import com.example.myquizbite.data.local.entity.toModel
import com.example.myquizbite.data.model.Difficulty
import com.example.myquizbite.data.model.ProfileStats
import com.example.myquizbite.data.model.Question
import com.example.myquizbite.data.model.Quiz
import com.example.myquizbite.data.model.QuizResultWithTitle
import com.example.myquizbite.data.model.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuizRepository(private val db: AppDatabase) {

    private val topicDao = db.topicDao()
    private val questionDao = db.questionDao()
    private val quizDao = db.quizDao()
    private val quizResultDao = db.quizResultDao()
    private val userProgressDao = db.userProgressDao()

    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (topicDao.getAll().isEmpty()) {
            topicDao.insertAll(SeedData.defaultTopicEntities())
            questionDao.insertAll(SeedData.defaultQuestions())
            quizDao.insertAll(SeedData.defaultQuizzes())
        }
    }

    suspend fun getTopics(): List<Topic> = withContext(Dispatchers.IO) {
        seedIfEmpty()
        topicDao.getAll().map { it.toModel() }
    }

    suspend fun getQuizzes(): List<Quiz> = withContext(Dispatchers.IO) {
        seedIfEmpty()
        quizDao.getAll().map { it.toModel() }
    }

    suspend fun getQuizzesByTopic(topicId: String): List<Quiz> = withContext(Dispatchers.IO) {
        quizDao.getByTopicId(topicId).map { it.toModel() }
    }

    suspend fun getQuizById(quizId: String): Quiz? = withContext(Dispatchers.IO) {
        quizDao.getById(quizId)?.toModel()
    }

    suspend fun getQuestionsForQuiz(quiz: Quiz): List<Question> = withContext(Dispatchers.IO) {
        questionDao.getByIds(quiz.questionIds).map { it.toModel() }
    }

    suspend fun getDailyChallenge(): Quiz? = withContext(Dispatchers.IO) {
        quizDao.getDailyChallenge()?.toModel()
    }

    /** Режим «Собеседование»: случайный набор вопросов по сложности с общим таймером */
    suspend fun getInterviewQuestions(count: Int, preferredDifficulty: Difficulty): List<Question> = withContext(Dispatchers.IO) {
        val all = questionDao.getAll().map { it.toModel() }
        val filtered = all.filter { it.difficulty == preferredDifficulty }.ifEmpty { all }
        filtered.shuffled().take(count)
    }

    suspend fun saveQuizResult(userId: String, quizId: String, correctCount: Int, totalCount: Int, timeSpentSeconds: Long) = withContext(Dispatchers.IO) {
        quizResultDao.insert(QuizResultEntity(userId = userId, quizId = quizId, correctCount = correctCount, totalCount = totalCount, timeSpentSeconds = timeSpentSeconds))
    }

    suspend fun recordAnswer(userId: String, questionId: String, correct: Boolean) = withContext(Dispatchers.IO) {
        userProgressDao.insert(UserProgressEntity(userId = userId, questionId = questionId, correct = correct))
    }

    suspend fun getResults(userId: String): List<QuizResultEntity> = withContext(Dispatchers.IO) {
        quizResultDao.getByUserId(userId)
    }

    suspend fun getCorrectRatio(userId: String): Float = withContext(Dispatchers.IO) {
        userProgressDao.getCorrectRatio(userId) ?: 0.5f
    }

    suspend fun getProfileStats(userId: String): ProfileStats = withContext(Dispatchers.IO) {
        val ratio = userProgressDao.getCorrectRatio(userId) ?: 0f
        val totalAnswered = userProgressDao.getTotalAnswered(userId)
        val quizzesCompleted = quizResultDao.getCountByUserId(userId)
        val results = quizResultDao.getByUserId(userId).take(15)
        val withTitles = results.map { r ->
            val quiz = quizDao.getById(r.quizId)?.toModel()
            QuizResultWithTitle(r, quiz?.title ?: r.quizId)
        }
        ProfileStats(correctRatio = ratio, totalAnswered = totalAnswered, quizzesCompleted = quizzesCompleted, recentResults = withTitles)
    }

    /** Адаптивная сложность: подбирает вопросы на основе correctRatio (0–1).
     * ratio > 0.8 → больше HARD, 0.5–0.8 → MEDIUM, < 0.5 → EASY */
    suspend fun getInterviewQuestionsAdaptive(count: Int, userId: String): List<Question> = withContext(Dispatchers.IO) {
        val ratio = userProgressDao.getCorrectRatio(userId) ?: 0.5f
        val preferredDifficulty = when {
            ratio >= 0.8f -> Difficulty.HARD
            ratio >= 0.5f -> Difficulty.MEDIUM
            else -> Difficulty.EASY
        }
        val all = questionDao.getAll().map { it.toModel() }
        val preferred = all.filter { it.difficulty == preferredDifficulty }
        val others = all.filter { it.difficulty != preferredDifficulty }
        val fromPreferred = if (preferred.isEmpty()) 0 else (count * 0.7).toInt().coerceIn(1, preferred.size)
        val fromOthers = (count - fromPreferred).coerceAtLeast(0)
        val result = (preferred.shuffled().take(fromPreferred) + others.shuffled().take(fromOthers)).shuffled()
        if (result.isEmpty()) all.shuffled().take(count) else result
    }
}
