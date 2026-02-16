package com.example.myquizbite.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myquizbite.data.model.Quiz
import com.example.myquizbite.data.model.Topic
import com.example.myquizbite.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizListScreen(
    topics: List<Topic>,
    quizzes: List<Quiz>,
    onBack: () -> Unit,
    onQuizClick: (String) -> Unit
) {
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    val filteredQuizzes = if (selectedTopic != null) quizzes.filter { it.topicId == selectedTopic } else quizzes.filter { !it.isDailyChallenge }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Библиотека викторин") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Topic chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            onClick = { selectedTopic = null },
                            label = { Text("Все") },
                            selected = selectedTopic == null
                        )
                    }
                    items(topics) { topic ->
                        FilterChip(
                            onClick = { selectedTopic = if (selectedTopic == topic.id) null else topic.id },
                            label = { Text(topic.name) },
                            selected = selectedTopic == topic.id,
                            leadingIcon = { Icon(topicIcon(topic.id), null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            items(filteredQuizzes) { quiz ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onQuizClick(quiz.id) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = difficultyColor(quiz.difficulty.level).copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(topicIcon(quiz.topicId), null, tint = difficultyColor(quiz.difficulty.level), modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(quiz.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(quiz.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${quiz.difficulty.displayName} · ${quiz.questionIds.size} вопросов · ${quiz.timePerQuestionSeconds}с/вопрос", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (filteredQuizzes.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Пока нет викторин по этой теме", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
