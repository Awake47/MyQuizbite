package com.example.myquizbite.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myquizbite.data.model.Question
import com.example.myquizbite.ui.viewmodel.InterviewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewRunScreen(
    state: InterviewState,
    onBack: () -> Unit,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onExit: () -> Unit
) {
    val q = state.questions.getOrNull(state.currentIndex)

    if (state.isFinished) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Собеседование завершено!", style = MaterialTheme.typography.headlineSmall)
            Text("Правильно: ${state.correctCount} из ${state.questions.size}", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onExit, modifier = Modifier.padding(top = 24.dp)) { Text("На главную") }
        }
        return
    }

    if (q == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Режим «Собеседование»") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Выход") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Вопрос ${state.currentIndex + 1} / ${state.questions.size}", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { (state.currentIndex + 1).toFloat() / state.questions.size }, modifier = Modifier.fillMaxWidth().height(4.dp))
            Spacer(Modifier.height(16.dp))
            Text(q.text, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            if (state.showExplanation) {
                val correct = state.selectedAnswer != null && q.isCorrect(state.selectedAnswer!!)
                Surface(
                    color = if (correct) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(if (correct) "Верно!" else "Неверно. Правильный ответ: ${q.options.getOrNull(q.correctIndex)}")
                        Spacer(Modifier.height(8.dp))
                        Text("Объяснение: ${q.explanation}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNext, modifier = Modifier.align(Alignment.End)) { Text("Далее") }
                    }
                }
            } else {
                q.options.forEachIndexed { index, option ->
                    OutlinedButton(
                        onClick = { onSelectAnswer(index) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) { Text(option, modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}
