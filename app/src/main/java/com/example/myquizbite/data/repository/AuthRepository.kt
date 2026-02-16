package com.example.myquizbite.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myquizbite.data.model.User
import com.example.myquizbite.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthRepository(private val context: Context) {

    private val userIdKey = stringPreferencesKey("user_id")
    private val userEmailKey = stringPreferencesKey("user_email")
    private val userDisplayNameKey = stringPreferencesKey("user_display_name")
    private val userRoleKey = stringPreferencesKey("user_role")

    private val currentUserFlow = MutableStateFlow<User?>(null)

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[userEmailKey] != null
    }

    val currentUser: Flow<User?> = currentUserFlow

    private fun resolveRole(email: String): UserRole = when {
        email.equals("admin@quizbyte.com", ignoreCase = true) -> UserRole.ADMIN
        email.equals("author@quizbyte.com", ignoreCase = true) -> UserRole.AUTHOR
        else -> UserRole.STUDENT
    }

    suspend fun login(email: String, password: String): Result<User> {
        if (email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Email и пароль (мин. 6 символов) обязательны"))
        }
        val role = resolveRole(email)
        val user = User(
            id = "local-${email.hashCode()}",
            email = email,
            displayName = if (role == UserRole.ADMIN) "Администратор" else email.substringBefore("@"),
            role = role
        )
        context.dataStore.edit { prefs ->
            prefs[userIdKey] = user.id
            prefs[userEmailKey] = user.email
            prefs[userDisplayNameKey] = user.displayName
            prefs[userRoleKey] = user.role.name
        }
        currentUserFlow.value = user
        return Result.success(user)
    }

    suspend fun register(email: String, password: String, displayName: String): Result<User> {
        if (email.isBlank() || password.length < 6) {
            return Result.failure(IllegalArgumentException("Email и пароль (мин. 6 символов) обязательны"))
        }
        val role = resolveRole(email)
        val user = User(
            id = "local-${email.hashCode()}",
            email = email,
            displayName = displayName.ifBlank { email.substringBefore("@") },
            role = role
        )
        context.dataStore.edit { prefs ->
            prefs[userIdKey] = user.id
            prefs[userEmailKey] = user.email
            prefs[userDisplayNameKey] = user.displayName
            prefs[userRoleKey] = user.role.name
        }
        currentUserFlow.value = user
        return Result.success(user)
    }

    suspend fun logout() {
        context.dataStore.edit { it.clear() }
        currentUserFlow.value = null
    }

    suspend fun loadStoredUser() {
        val prefs = context.dataStore.data.first()
        val id = prefs[userIdKey] ?: return
        val email = prefs[userEmailKey] ?: return
        val name = prefs[userDisplayNameKey] ?: email.substringBefore("@")
        val role = try { UserRole.valueOf(prefs[userRoleKey] ?: "STUDENT") } catch (_: Exception) { UserRole.STUDENT }
        currentUserFlow.value = User(id = id, email = email, displayName = name, role = role)
    }

    fun getCurrentUserId(): String? = currentUserFlow.value?.id
}
