package com.example.myquizbite.data.model

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val condition: (stats: AchievementCheckData) -> Boolean
)

data class AchievementCheckData(
    val totalAnswered: Int,
    val correctRatio: Float,
    val quizzesCompleted: Int,
    val streakDays: Int,
    val topicsStarted: Int
)

object AchievementDefinitions {
    val all = listOf(
        AchievementDefinition("first_quiz", "Первые шаги", "Пройди первую викторину", "rocket") { it.quizzesCompleted >= 1 },
        AchievementDefinition("quiz_5", "Пять из пяти", "Пройди 5 викторин", "star") { it.quizzesCompleted >= 5 },
        AchievementDefinition("quiz_10", "Десятка", "Пройди 10 викторин", "trophy") { it.quizzesCompleted >= 10 },
        AchievementDefinition("quiz_25", "Четвертак", "Пройди 25 викторин", "diamond") { it.quizzesCompleted >= 25 },
        AchievementDefinition("answers_50", "Полсотни", "Ответь на 50 вопросов", "chat") { it.totalAnswered >= 50 },
        AchievementDefinition("answers_200", "Двести!", "Ответь на 200 вопросов", "fire") { it.totalAnswered >= 200 },
        AchievementDefinition("accuracy_80", "Снайпер", "Точность > 80%", "target") { it.totalAnswered >= 10 && it.correctRatio >= 0.8f },
        AchievementDefinition("accuracy_95", "Перфекционист", "Точность > 95%", "bullseye") { it.totalAnswered >= 20 && it.correctRatio >= 0.95f },
        AchievementDefinition("streak_3", "Три дня подряд", "Стрик 3 дня", "flame") { it.streakDays >= 3 },
        AchievementDefinition("streak_7", "Неделя силы", "Стрик 7 дней", "lightning") { it.streakDays >= 7 },
        AchievementDefinition("streak_30", "Месяц дисциплины", "Стрик 30 дней", "crown") { it.streakDays >= 30 },
        AchievementDefinition("topics_3", "Мультиязычник", "Начни 3 разные темы", "globe") { it.topicsStarted >= 3 },
        AchievementDefinition("topics_6", "Полиглот", "Начни 6 разных тем", "books") { it.topicsStarted >= 6 }
    )
}
