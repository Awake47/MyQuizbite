package com.example.myquizbite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myquizbite.data.local.entity.AchievementEntity
import com.example.myquizbite.data.local.entity.QuestionEntity
import com.example.myquizbite.data.local.entity.QuizEntity
import com.example.myquizbite.data.local.entity.QuizResultEntity
import com.example.myquizbite.data.local.entity.TopicEntity
import com.example.myquizbite.data.local.entity.UserProgressEntity

@Database(
    entities = [
        TopicEntity::class,
        QuestionEntity::class,
        QuizEntity::class,
        QuizResultEntity::class,
        UserProgressEntity::class,
        AchievementEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizDao(): QuizDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun achievementDao(): AchievementDao
}
