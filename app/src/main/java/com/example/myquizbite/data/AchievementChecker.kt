package com.example.myquizbite.data

import com.example.myquizbite.data.local.AchievementDao
import com.example.myquizbite.data.local.QuizResultDao
import com.example.myquizbite.data.local.UserProgressDao
import com.example.myquizbite.data.local.entity.AchievementEntity
import com.example.myquizbite.data.model.AchievementCheckData
import com.example.myquizbite.data.model.AchievementDefinitions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AchievementChecker(
    private val achievementDao: AchievementDao,
    private val userProgressDao: UserProgressDao,
    private val quizResultDao: QuizResultDao,
    private val streakManager: StreakManager
) {
    /** Проверяет и разблокирует новые достижения. Возвращает список НОВЫХ. */
    suspend fun checkAndUnlock(userId: String): List<AchievementEntity> = withContext(Dispatchers.IO) {
        val totalAnswered = userProgressDao.getTotalAnswered(userId)
        val correctRatio = userProgressDao.getCorrectRatio(userId) ?: 0f
        val quizzesCompleted = quizResultDao.getCountByUserId(userId)
        val streakDays = streakManager.getStreak()
        val topicsStarted = userProgressDao.getDistinctTopicsCount(userId)

        val data = AchievementCheckData(
            totalAnswered = totalAnswered,
            correctRatio = correctRatio,
            quizzesCompleted = quizzesCompleted,
            streakDays = streakDays,
            topicsStarted = topicsStarted
        )

        val newAchievements = mutableListOf<AchievementEntity>()
        for (def in AchievementDefinitions.all) {
            if (achievementDao.hasAchievement(userId, def.id) == 0 && def.condition(data)) {
                val entity = AchievementEntity(
                    id = "${def.id}_$userId",
                    userId = userId,
                    title = def.title,
                    description = def.description,
                    iconName = def.iconName
                )
                achievementDao.insert(entity)
                newAchievements.add(entity)
            }
        }
        newAchievements
    }
}
