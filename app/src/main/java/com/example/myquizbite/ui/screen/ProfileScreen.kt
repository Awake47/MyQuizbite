package com.example.myquizbite.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myquizbite.data.ThemeMode
import com.example.myquizbite.data.local.entity.AchievementEntity
import com.example.myquizbite.data.model.ProfileStats
import com.example.myquizbite.data.model.User
import com.example.myquizbite.data.model.UserRole
import com.example.myquizbite.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    stats: ProfileStats?,
    streak: Int,
    totalXp: Int,
    achievements: List<AchievementEntity>,
    currentTheme: ThemeMode,
    onThemeChanged: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // User card
            user?.let {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape, modifier = Modifier.size(56.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(it.displayName.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(it.displayName, style = MaterialTheme.typography.headlineSmall)
                                    if (it.role != UserRole.STUDENT) {
                                        val (badgeText, badgeColor) = when (it.role) {
                                            UserRole.ADMIN -> "Админ" to MaterialTheme.colorScheme.error
                                            UserRole.AUTHOR -> "Автор" to MaterialTheme.colorScheme.tertiary
                                            else -> "" to MaterialTheme.colorScheme.primary
                                        }
                                        Surface(
                                            color = badgeColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                badgeText,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                        }
                                    }
                                }
                                Text(it.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // XP + Streak + Level
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Icons.Default.Bolt, "$totalXp", "XP", XpGold, Modifier.weight(1f))
                    StatCard(Icons.Default.LocalFireDepartment, "$streak", "дней стрик", StreakOrange, Modifier.weight(1f))
                    StatCard(Icons.AutoMirrored.Filled.TrendingUp, "${user?.level ?: 1}", "уровень", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                }
            }

            // Stats
            stats?.let { s ->
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Статистика", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                StatItem("Точность", "${(s.correctRatio * 100).toInt()}%")
                                StatItem("Ответов", "${s.totalAnswered}")
                                StatItem("Квизов", "${s.quizzesCompleted}")
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { s.correctRatio },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = if (s.correctRatio >= 0.7f) CorrectGreen else if (s.correctRatio >= 0.5f) XpGold else WrongRed,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }

            // Achievements
            if (achievements.isNotEmpty()) {
                item { Text("Достижения (${achievements.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(achievements) { a ->
                            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.width(120.dp)) {
                                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(color = AchievementPurple.copy(alpha = 0.15f), shape = CircleShape, modifier = Modifier.size(40.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Star, null, tint = AchievementPurple, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(a.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(a.description, style = MaterialTheme.typography.labelSmall, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Recent results
            stats?.let { s ->
                if (s.recentResults.isNotEmpty()) {
                    item { Text("Последние результаты", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(s.recentResults) { r ->
                        val date = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(r.result.timestamp))
                        val ratio = if (r.result.totalCount > 0) r.result.correctCount.toFloat() / r.result.totalCount else 0f
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = (if (ratio >= 0.7f) CorrectGreen else WrongRed).copy(alpha = 0.12f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("${(ratio * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (ratio >= 0.7f) CorrectGreen else WrongRed)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(r.quizTitle, style = MaterialTheme.typography.titleSmall)
                                    Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${r.result.correctCount}/${r.result.totalCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Theme selector
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Тема оформления", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                val selected = currentTheme == mode
                                val icon = when (mode) {
                                    ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                }
                                FilterChip(
                                    onClick = { onThemeChanged(mode) },
                                    label = { Text(mode.displayName) },
                                    selected = selected,
                                    leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти из аккаунта")
                }
            }
        }
    }
}

@Composable
private fun StatCard(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
