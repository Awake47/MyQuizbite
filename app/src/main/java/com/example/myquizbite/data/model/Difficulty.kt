package com.example.myquizbite.data.model

enum class Difficulty(val displayName: String, val level: Int) {
    EASY("Легко", 1),
    MEDIUM("Средне", 2),
    HARD("Сложно", 3);

    companion object {
        fun fromLevel(level: Int) = entries.find { it.level == level } ?: MEDIUM
    }
}
