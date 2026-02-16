package com.example.myquizbite.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myquizbite.QuizBiteApp
import com.example.myquizbite.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepo = (application as QuizBiteApp).authRepository

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            authRepo.currentUser.collect { _user.value = it }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            authRepo.login(email, password)
                .onSuccess { _user.value = it }
                .onFailure { _authError.value = it.message ?: "Ошибка входа" }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authError.value = null
            authRepo.register(email, password, displayName)
                .onSuccess { _user.value = it }
                .onFailure { _authError.value = it.message ?: "Ошибка регистрации" }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _user.value = null
        }
    }

    fun clearError() { _authError.value = null }
}
