package com.example.myquizbite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myquizbite.QuizBiteApp
import com.example.myquizbite.data.model.Difficulty
import com.example.myquizbite.data.model.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InterviewState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val showExplanation: Boolean = false,
    val correctCount: Int = 0,
    val totalTimeSeconds: Int = 0,
    val isFinished: Boolean = false
)

class InterviewViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as QuizBiteApp
    private val quizRepo = app.quizRepository
    private val authRepo = app.authRepository
    private val streakMgr = app.streakManager

    private val _state = MutableStateFlow(InterviewState())
    val state: StateFlow<InterviewState> = _state.asStateFlow()

    fun startInterview(difficulty: Difficulty, questionCount: Int = 7) {
        viewModelScope.launch {
            val questions = quizRepo.getInterviewQuestions(questionCount, difficulty)
            _state.value = InterviewState(questions = questions)
        }
    }

    fun startInterviewAdaptive(userId: String, questionCount: Int = 7) {
        viewModelScope.launch {
            val questions = quizRepo.getInterviewQuestionsAdaptive(questionCount, userId)
            _state.value = InterviewState(questions = questions)
        }
    }

    fun selectAnswer(index: Int) {
        if (_state.value.showExplanation) return
        val s = _state.value
        val correct = s.questions.getOrNull(s.currentIndex)?.isCorrect?.invoke(index) == true
        viewModelScope.launch {
            authRepo.getCurrentUserId()?.let { uid ->
                quizRepo.recordAnswer(uid, s.questions[s.currentIndex].id, correct)
            }
            streakMgr.addXp(if (correct) 10 else 2)
            streakMgr.recordPractice()
        }
        _state.value = s.copy(selectedAnswer = index, showExplanation = true, correctCount = s.correctCount + if (correct) 1 else 0)
    }

    fun nextQuestion() {
        val s = _state.value
        if (s.currentIndex + 1 >= s.questions.size) {
            viewModelScope.launch { streakMgr.addXp(25) }
            _state.value = s.copy(isFinished = true)
        } else {
            _state.value = s.copy(currentIndex = s.currentIndex + 1, selectedAnswer = null, showExplanation = false)
        }
    }

    fun reset() { _state.value = InterviewState() }
}
