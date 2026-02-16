package com.example.myquizbite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myquizbite.QuizBiteApp
import com.example.myquizbite.data.local.entity.AchievementEntity
import com.example.myquizbite.data.model.Quiz
import com.example.myquizbite.data.model.Topic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as QuizBiteApp
    private val quizRepo = app.quizRepository
    private val streakMgr = app.streakManager
    private val achievementChecker = app.achievementChecker
    private val authRepo = app.authRepository

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _dailyQuiz = MutableStateFlow<Quiz?>(null)
    val dailyQuiz: StateFlow<Quiz?> = _dailyQuiz.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _totalXp = MutableStateFlow(0)
    val totalXp: StateFlow<Int> = _totalXp.asStateFlow()

    private val _newAchievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val newAchievements: StateFlow<List<AchievementEntity>> = _newAchievements.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            _topics.value = quizRepo.getTopics()
            _quizzes.value = quizRepo.getQuizzes()
            _dailyQuiz.value = quizRepo.getDailyChallenge()
            _streak.value = streakMgr.getStreak()
            _totalXp.value = streakMgr.getTotalXp()
            authRepo.getCurrentUserId()?.let { uid ->
                _newAchievements.value = achievementChecker.checkAndUnlock(uid)
            }
            _loading.value = false
        }
    }

    fun clearNewAchievements() { _newAchievements.value = emptyList() }
}
