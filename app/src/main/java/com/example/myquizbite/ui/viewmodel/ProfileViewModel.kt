package com.example.myquizbite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myquizbite.QuizBiteApp
import com.example.myquizbite.data.ThemeMode
import com.example.myquizbite.data.local.entity.AchievementEntity
import com.example.myquizbite.data.model.ProfileStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as QuizBiteApp
    private val quizRepo = app.quizRepository
    private val authRepo = app.authRepository
    private val streakMgr = app.streakManager
    private val themeMgr = app.themeManager

    private val _stats = MutableStateFlow<ProfileStats?>(null)
    val stats: StateFlow<ProfileStats?> = _stats.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _totalXp = MutableStateFlow(0)
    val totalXp: StateFlow<Int> = _totalXp.asStateFlow()

    private val _achievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val achievements: StateFlow<List<AchievementEntity>> = _achievements.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _streak.value = streakMgr.getStreak()
            _totalXp.value = streakMgr.getTotalXp()
            authRepo.getCurrentUserId()?.let { uid ->
                _stats.value = quizRepo.getProfileStats(uid)
                launch(Dispatchers.IO) {
                    _achievements.value = app.database.achievementDao().getByUser(uid)
                }
            }
            themeMgr.themeMode.collect { _themeMode.value = it }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            _themeMode.value = mode
            themeMgr.setThemeMode(mode)
        }
    }
}
