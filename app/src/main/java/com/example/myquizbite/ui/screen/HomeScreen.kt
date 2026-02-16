package com.example.myquizbite.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myquizbite.data.local.entity.AchievementEntity
import com.example.myquizbite.data.model.Quiz
import com.example.myquizbite.data.model.Topic
import com.example.myquizbite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    topics: List<Topic>,
    quizzes: List<Quiz>,
    dailyQuiz: Quiz?,
    loading: Boolean,
    streak: Int,
    totalXp: Int,
    isAdmin: Boolean = false,
    newAchievements: List<AchievementEntity>,
    onRefresh: () -> Unit,
    onQuizClick: (String) -> Unit,
    onProfile: () -> Unit,
    onInterview: () -> Unit,
    onDuel: () -> Unit,
    onDailyChallenge: () -> Unit,
    onQuizList: () -> Unit,
    onAdminPanel: () -> Unit = {},
    onDismissAchievements: () -> Unit
) {
    // Achievement popup
    if (newAchievements.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDismissAchievements,
            icon = { Icon(Icons.Default.EmojiEvents, null, tint = XpGold, modifier = Modifier.size(48.dp)) },
            title = { Text("Новые достижения!", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    newAchievements.forEach { a ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = AchievementPurple.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Star, null, tint = AchievementPurple, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(a.title, style = MaterialTheme.typography.titleSmall)
                                Text(a.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismissAchievements) { Text("Отлично!") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "QuizByte",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onAdminPanel) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Админ-панель", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(onClick = onProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                }
            )
        }
    ) { padding ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Welcome + Stats bar
            item {
                Spacer(Modifier.height(4.dp))
                Text("Привет, $userName!", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip(Icons.Default.LocalFireDepartment, "$streak", "стрик", StreakOrange, Modifier.weight(1f))
                    StatChip(Icons.Default.Bolt, "$totalXp", "XP", XpGold, Modifier.weight(1f))
                    StatChip(Icons.Default.Quiz, "${quizzes.size}", "квизов", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                }
            }

            // Daily challenge
            dailyQuiz?.let { quiz ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onDailyChallenge),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(48.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Ежедневный челлендж", style = MaterialTheme.typography.titleMedium)
                                Text("${quiz.questionIds.size} вопросов из разных тем", style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                }
            }

            // Mode cards
            item {
                Text("Режимы", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModeCard("Библиотека", "По темам", Icons.AutoMirrored.Filled.MenuBook, onQuizList, Modifier.weight(1f))
                    ModeCard("Собеседование", "С таймером", Icons.Default.WorkspacePremium, onInterview, Modifier.weight(1f))
                    ModeCard("Дуэль", "PvP", Icons.Default.SportsEsports, onDuel, Modifier.weight(1f))
                }
            }

            // Topics
            if (topics.isNotEmpty()) {
                item {
                    Text("Темы", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(topics) { topic ->
                            FilterChip(
                                onClick = { onQuizList() },
                                label = { Text(topic.name) },
                                selected = false,
                                leadingIcon = { Icon(topicIcon(topic.id), null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }
            }

            // Recent quizzes
            item {
                Text("Все викторины", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            }
            items(quizzes.filter { !it.isDailyChallenge }.take(8)) { quiz ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onQuizClick(quiz.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = difficultyColor(quiz.difficulty.level).copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(topicIcon(quiz.topicId), null, tint = difficultyColor(quiz.difficulty.level), modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(quiz.title, style = MaterialTheme.typography.titleSmall)
                            Text("${quiz.difficulty.displayName} · ${quiz.questionIds.size} вопросов", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(icon: ImageVector, value: String, label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ModeCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun topicIcon(topicId: String): ImageVector = when (topicId) {
    "python" -> Icons.Default.Code
    "kotlin" -> Icons.Default.PhoneAndroid
    "javascript" -> Icons.Default.Language
    "sql" -> Icons.Default.Storage
    "algorithms" -> Icons.Default.Memory
    "oop" -> Icons.Default.AccountTree
    "patterns" -> Icons.Default.Extension
    "git" -> Icons.Default.ForkRight
    "system_design" -> Icons.Default.Hub
    else -> Icons.Default.Code
}

fun difficultyColor(level: Int): androidx.compose.ui.graphics.Color = when (level) {
    1 -> CorrectGreen
    2 -> XpGold
    3 -> WrongRed
    else -> CorrectGreen
}
