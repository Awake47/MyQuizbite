package com.example.myquizbite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myquizbite.QuizBiteApp
import com.example.myquizbite.data.local.entity.QuestionEntity
import com.example.myquizbite.data.local.entity.QuizEntity
import com.example.myquizbite.data.local.entity.TopicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

data class AdminStats(
    val totalQuestions: Int = 0,
    val totalQuizzes: Int = 0,
    val totalTopics: Int = 0,
    val questionsByTopic: Map<String, Int> = emptyMap()
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as QuizBiteApp
    private val db = app.database

    private val _stats = MutableStateFlow(AdminStats())
    val stats: StateFlow<AdminStats> = _stats.asStateFlow()

    private val _questions = MutableStateFlow<List<QuestionEntity>>(emptyList())
    val questions: StateFlow<List<QuestionEntity>> = _questions.asStateFlow()

    private val _quizzes = MutableStateFlow<List<QuizEntity>>(emptyList())
    val quizzes: StateFlow<List<QuizEntity>> = _quizzes.asStateFlow()

    private val _topics = MutableStateFlow<List<TopicEntity>>(emptyList())
    val topics: StateFlow<List<TopicEntity>> = _topics.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val topicList = db.topicDao().getAll()
            val questionList = db.questionDao().getAll()
            val quizList = db.quizDao().getAll()

            val byTopic = topicList.associate { t ->
                t.name to db.questionDao().getCountByTopic(t.id)
            }

            _topics.value = topicList
            _questions.value = questionList
            _quizzes.value = quizList
            _stats.value = AdminStats(
                totalQuestions = questionList.size,
                totalQuizzes = quizList.size,
                totalTopics = topicList.size,
                questionsByTopic = byTopic
            )
        }
    }

    fun deleteQuestion(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.questionDao().deleteById(id)
            _message.value = "Вопрос удалён"
            load()
        }
    }

    fun deleteQuiz(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.quizDao().deleteById(id)
            _message.value = "Квиз удалён"
            load()
        }
    }

    fun addQuestion(
        topicId: String,
        text: String,
        options: List<String>,
        correctIndex: Int,
        explanation: String,
        difficulty: String,
        codeSnippet: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = "q_admin_${System.currentTimeMillis()}"
            val entity = QuestionEntity(
                id = id,
                topicId = topicId,
                text = text,
                optionsJson = JSONArray(options).toString(),
                correctIndex = correctIndex,
                explanation = explanation,
                difficulty = difficulty,
                codeSnippet = codeSnippet?.ifBlank { null }
            )
            db.questionDao().insert(entity)
            _message.value = "Вопрос добавлен"
            load()
        }
    }

    fun addQuiz(
        topicId: String,
        title: String,
        description: String,
        difficulty: String,
        questionIds: List<String>,
        timePerQuestion: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = "quiz_admin_${System.currentTimeMillis()}"
            val entity = QuizEntity(
                id = id,
                topicId = topicId,
                title = title,
                description = description,
                difficulty = difficulty,
                questionIdsJson = JSONArray(questionIds).toString(),
                timePerQuestionSeconds = timePerQuestion,
                isDailyChallenge = false
            )
            db.quizDao().insert(entity)
            _message.value = "Квиз добавлен"
            load()
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
