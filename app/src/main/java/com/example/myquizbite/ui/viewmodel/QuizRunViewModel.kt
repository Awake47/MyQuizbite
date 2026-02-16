package com.example.myquizbite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myquizbite.QuizBiteApp
import com.example.myquizbite.data.model.Question
import com.example.myquizbite.data.model.Quiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizRunState(
    val quiz: Quiz? = null,
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val showExplanation: Boolean = false,
    val correctCount: Int = 0,
    val timeLeftSeconds: Int = 0,
    val isFinished: Boolean = false,
    val error: String? = null
)

class QuizRunViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as QuizBiteApp
    private val quizRepo = app.quizRepository
    private val authRepo = app.authRepository
    private val streakMgr = app.streakManager

    private val _state = MutableStateFlow(QuizRunState())
    val state: StateFlow<QuizRunState> = _state.asStateFlow()

    fun startQuiz(quizId: String) {
        viewModelScope.launch {
            val quiz = quizRepo.getQuizById(quizId) ?: run {
                _state.value = _state.value.copy(error = "Викторина не найдена")
                return@launch
            }
            val questions = quizRepo.getQuestionsForQuiz(quiz)
            if (questions.isEmpty()) {
                _state.value = _state.value.copy(error = "Нет вопросов")
                return@launch
            }
            _state.value = QuizRunState(
                quiz = quiz,
                questions = questions,
                timeLeftSeconds = quiz.timePerQuestionSeconds
            )
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
            // XP: +10 за правильный, +2 за неправильный (за попытку)
            streakMgr.addXp(if (correct) 10 else 2)
            streakMgr.recordPractice()
        }
        _state.value = s.copy(
            selectedAnswer = index,
            showExplanation = true,
            correctCount = s.correctCount + if (correct) 1 else 0
        )
    }

    fun nextQuestion() {
        val s = _state.value
        if (s.currentIndex + 1 >= s.questions.size) {
            viewModelScope.launch {
                authRepo.getCurrentUserId()?.let { uid ->
                    quizRepo.saveQuizResult(uid, s.quiz!!.id, s.correctCount, s.questions.size, 0L)
                }
                // Bonus XP for completing quiz
                streakMgr.addXp(25)
            }
            _state.value = s.copy(isFinished = true)
        } else {
            _state.value = s.copy(
                currentIndex = s.currentIndex + 1,
                selectedAnswer = null,
                showExplanation = false,
                timeLeftSeconds = s.quiz!!.timePerQuestionSeconds
            )
        }
    }

    fun setTimeLeft(seconds: Int) {
        _state.value = _state.value.copy(timeLeftSeconds = seconds)
    }

    fun reset() {
        _state.value = QuizRunState()
    }
}
