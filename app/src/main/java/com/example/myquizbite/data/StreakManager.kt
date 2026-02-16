package com.example.myquizbite.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.Calendar

private val Context.streakStore: DataStore<Preferences> by preferencesDataStore(name = "streak")

class StreakManager(private val context: Context) {
    private val streakKey = intPreferencesKey("streak_count")
    private val lastDateKey = longPreferencesKey("last_practice_date")
    private val totalXpKey = intPreferencesKey("total_xp")

    suspend fun getStreak(): Int = context.streakStore.data.first()[streakKey] ?: 0
    suspend fun getTotalXp(): Int = context.streakStore.data.first()[totalXpKey] ?: 0

    suspend fun addXp(amount: Int) {
        context.streakStore.edit { prefs ->
            val current = prefs[totalXpKey] ?: 0
            prefs[totalXpKey] = current + amount
        }
    }

    /** Вызывать при завершении викторины / ответе на вопрос */
    suspend fun recordPractice() {
        val today = todayMillis()
        context.streakStore.edit { prefs ->
            val lastDate = prefs[lastDateKey] ?: 0L
            val currentStreak = prefs[streakKey] ?: 0

            when {
                isSameDay(lastDate, today) -> { /* уже засчитано */ }
                isYesterday(lastDate, today) -> {
                    prefs[streakKey] = currentStreak + 1
                    prefs[lastDateKey] = today
                }
                else -> {
                    prefs[streakKey] = 1
                    prefs[lastDateKey] = today
                }
            }
        }
    }

    private fun todayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun isSameDay(a: Long, b: Long): Boolean = a == b
    private fun isYesterday(last: Long, today: Long): Boolean = (today - last) == 86_400_000L
}
