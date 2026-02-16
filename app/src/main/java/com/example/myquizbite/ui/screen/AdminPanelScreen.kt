package com.example.myquizbite.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myquizbite.data.local.entity.QuestionEntity
import com.example.myquizbite.data.local.entity.QuizEntity
import com.example.myquizbite.data.local.entity.TopicEntity
import com.example.myquizbite.ui.theme.*
import com.example.myquizbite.ui.viewmodel.AdminStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    stats: AdminStats,
    questions: List<QuestionEntity>,
    quizzes: List<QuizEntity>,
    topics: List<TopicEntity>,
    message: String?,
    onDeleteQuestion: (String) -> Unit,
    onDeleteQuiz: (String) -> Unit,
    onAddQuestion: (topicId: String, text: String, options: List<String>, correctIndex: Int, explanation: String, difficulty: String, codeSnippet: String?) -> Unit,
    onAddQuiz: (topicId: String, title: String, description: String, difficulty: String, questionIds: List<String>, timePerQuestion: Int) -> Unit,
    onDismissMessage: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Обзор", "Вопросы", "Квизы", "Добавить")

    if (message != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = { TextButton(onClick = onDismissMessage) { Text("OK") } }
        ) { Text(message) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.error)
                        Text("Админ-панель")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1) }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewTab(stats)
                1 -> QuestionsTab(questions, topics, onDeleteQuestion)
                2 -> QuizzesTab(quizzes, onDeleteQuiz)
                3 -> AddTab(topics, questions, onAddQuestion, onAddQuiz)
            }
        }
    }
}

@Composable
private fun OverviewTab(stats: AdminStats) {
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Статистика контента", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OverviewCard("Вопросов", "${stats.totalQuestions}", Icons.Default.QuestionMark, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                OverviewCard("Квизов", "${stats.totalQuizzes}", Icons.Default.Quiz, XpGold, Modifier.weight(1f))
                OverviewCard("Тем", "${stats.totalTopics}", Icons.Default.Category, CorrectGreen, Modifier.weight(1f))
            }
        }
        if (stats.questionsByTopic.isNotEmpty()) {
            item {
                Text("Вопросов по темам", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(stats.questionsByTopic.entries.toList()) { (topic, count) ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(topic, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "$count",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier
) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(color = color.copy(alpha = 0.15f), shape = CircleShape, modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuestionsTab(
    questions: List<QuestionEntity>,
    topics: List<TopicEntity>,
    onDelete: (String) -> Unit
) {
    var filterTopic by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить вопрос?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(showDeleteDialog!!)
                    showDeleteDialog = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Отмена") } }
        )
    }

    val filtered = if (filterTopic != null) questions.filter { it.topicId == filterTopic } else questions

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Text("Всего: ${questions.size} · Показано: ${filtered.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = filterTopic == null,
                    onClick = { filterTopic = null },
                    shape = SegmentedButtonDefaults.itemShape(0, topics.size + 1)
                ) { Text("Все", maxLines = 1) }
                topics.forEachIndexed { i, t ->
                    SegmentedButton(
                        selected = filterTopic == t.id,
                        onClick = { filterTopic = t.id },
                        shape = SegmentedButtonDefaults.itemShape(i + 1, topics.size + 1)
                    ) { Text(t.name, maxLines = 1, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }

        items(filtered) { q ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(q.text, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(q.topicId, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(q.difficulty, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(q.id, Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    IconButton(onClick = { showDeleteDialog = q.id }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizzesTab(
    quizzes: List<QuizEntity>,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить квиз?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(showDeleteDialog!!)
                    showDeleteDialog = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Отмена") } }
        )
    }

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            Text("Всего квизов: ${quizzes.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(quizzes) { quiz ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(quiz.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${quiz.difficulty} · ${quiz.topicId} · ${quiz.timePerQuestionSeconds}с/вопрос",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "ID: ${quiz.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = quiz.id }) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTab(
    topics: List<TopicEntity>,
    questions: List<QuestionEntity>,
    onAddQuestion: (String, String, List<String>, Int, String, String, String?) -> Unit,
    onAddQuiz: (String, String, String, String, List<String>, Int) -> Unit
) {
    var addMode by remember { mutableIntStateOf(0) }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = addMode == 0,
                    onClick = { addMode = 0 },
                    label = { Text("Новый вопрос") },
                    leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(18.dp)) }
                )
                FilterChip(
                    selected = addMode == 1,
                    onClick = { addMode = 1 },
                    label = { Text("Новый квиз") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null, Modifier.size(18.dp)) }
                )
            }
        }

        if (addMode == 0) {
            item { AddQuestionForm(topics, onAddQuestion) }
        } else {
            item { AddQuizForm(topics, questions, onAddQuiz) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddQuestionForm(
    topics: List<TopicEntity>,
    onAdd: (String, String, List<String>, Int, String, String, String?) -> Unit
) {
    var topicId by remember { mutableStateOf(topics.firstOrNull()?.id ?: "") }
    var text by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }
    var opt4 by remember { mutableStateOf("") }
    var correctIndex by remember { mutableIntStateOf(0) }
    var explanation by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("MEDIUM") }
    var codeSnippet by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var diffExpanded by remember { mutableStateOf(false) }

    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Добавить вопрос", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = topics.find { it.id == topicId }?.name ?: topicId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тема") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    topics.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.name) },
                            onClick = { topicId = t.id; expanded = false }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = diffExpanded, onExpandedChange = { diffExpanded = it }) {
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Сложность") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(diffExpanded) }
                )
                ExposedDropdownMenu(expanded = diffExpanded, onDismissRequest = { diffExpanded = false }) {
                    listOf("EASY", "MEDIUM", "HARD").forEach { d ->
                        DropdownMenuItem(
                            text = { Text(d) },
                            onClick = { difficulty = d; diffExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Текст вопроса") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            OutlinedTextField(value = codeSnippet, onValueChange = { codeSnippet = it }, label = { Text("Код (необязательно)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            Text("Варианты ответов:", style = MaterialTheme.typography.labelLarge)
            OptionRow(0, opt1, { opt1 = it }, correctIndex) { correctIndex = 0 }
            OptionRow(1, opt2, { opt2 = it }, correctIndex) { correctIndex = 1 }
            OptionRow(2, opt3, { opt3 = it }, correctIndex) { correctIndex = 2 }
            OptionRow(3, opt4, { opt4 = it }, correctIndex) { correctIndex = 3 }

            OutlinedTextField(value = explanation, onValueChange = { explanation = it }, label = { Text("Объяснение") }, modifier = Modifier.fillMaxWidth(), minLines = 2)

            Button(
                onClick = {
                    if (text.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank()) {
                        val options = listOf(opt1, opt2, opt3, opt4).filter { it.isNotBlank() }
                        onAdd(topicId, text, options, correctIndex, explanation, difficulty, codeSnippet.ifBlank { null })
                        text = ""; opt1 = ""; opt2 = ""; opt3 = ""; opt4 = ""; explanation = ""; codeSnippet = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Добавить вопрос")
            }
        }
    }
}

@Composable
private fun OptionRow(index: Int, value: String, onChange: (String) -> Unit, selected: Int, onSelect: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected == index, onClick = onSelect)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text("Вариант ${index + 1}") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddQuizForm(
    topics: List<TopicEntity>,
    questions: List<QuestionEntity>,
    onAdd: (String, String, String, String, List<String>, Int) -> Unit
) {
    var topicId by remember { mutableStateOf(topics.firstOrNull()?.id ?: "") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("MEDIUM") }
    var timePerQ by remember { mutableStateOf("60") }
    var expanded by remember { mutableStateOf(false) }
    var diffExpanded by remember { mutableStateOf(false) }
    val selectedQuestionIds = remember { mutableStateListOf<String>() }

    val topicQuestions = questions.filter { it.topicId == topicId }

    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Создать квиз", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = topics.find { it.id == topicId }?.name ?: topicId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тема") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    topics.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.name) },
                            onClick = {
                                topicId = t.id
                                expanded = false
                                selectedQuestionIds.clear()
                            }
                        )
                    }
                }
            }

            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") }, modifier = Modifier.fillMaxWidth())

            ExposedDropdownMenuBox(expanded = diffExpanded, onExpandedChange = { diffExpanded = it }) {
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Сложность") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(diffExpanded) }
                )
                ExposedDropdownMenu(expanded = diffExpanded, onDismissRequest = { diffExpanded = false }) {
                    listOf("EASY", "MEDIUM", "HARD").forEach { d ->
                        DropdownMenuItem(text = { Text(d) }, onClick = { difficulty = d; diffExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = timePerQ,
                onValueChange = { timePerQ = it.filter { c -> c.isDigit() } },
                label = { Text("Секунд на вопрос") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Text("Выберите вопросы (${selectedQuestionIds.size}):", style = MaterialTheme.typography.labelLarge)
            topicQuestions.forEach { q ->
                val checked = q.id in selectedQuestionIds
                Row(
                    Modifier.fillMaxWidth().clickable {
                        if (checked) selectedQuestionIds.remove(q.id) else selectedQuestionIds.add(q.id)
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = checked, onCheckedChange = {
                        if (it) selectedQuestionIds.add(q.id) else selectedQuestionIds.remove(q.id)
                    })
                    Text(q.text, style = MaterialTheme.typography.bodySmall, maxLines = 2, modifier = Modifier.weight(1f))
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && selectedQuestionIds.isNotEmpty()) {
                        onAdd(topicId, title, description, difficulty, selectedQuestionIds.toList(), timePerQ.toIntOrNull() ?: 60)
                        title = ""; description = ""; selectedQuestionIds.clear()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = title.isNotBlank() && selectedQuestionIds.isNotEmpty()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Создать квиз")
            }
        }
    }
}
