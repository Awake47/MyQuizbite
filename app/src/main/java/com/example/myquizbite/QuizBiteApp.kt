package com.example.myquizbite

import android.app.Application
import androidx.room.Room
import com.example.myquizbite.data.AchievementChecker
import com.example.myquizbite.data.StreakManager
import com.example.myquizbite.data.ThemeManager
import com.example.myquizbite.data.local.AppDatabase
import com.example.myquizbite.data.repository.AuthRepository
import com.example.myquizbite.data.repository.QuizRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuizBiteApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "quizbite_db")
            .fallbackToDestructiveMigration()
            .build()
    }
    val authRepository: AuthRepository by lazy { AuthRepository(this) }
    val quizRepository: QuizRepository by lazy { QuizRepository(database) }
    val streakManager: StreakManager by lazy { StreakManager(this) }
    val themeManager: ThemeManager by lazy { ThemeManager(this) }
    val achievementChecker: AchievementChecker by lazy {
        AchievementChecker(database.achievementDao(), database.userProgressDao(), database.quizResultDao(), streakManager)
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            quizRepository.seedIfEmpty()
            authRepository.loadStoredUser()
        }
    }
}
