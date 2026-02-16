package com.example.myquizbite.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myquizbite.ui.theme.*
import com.example.myquizbite.ui.viewmodel.QuizRunState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizRunScreen(
    state: QuizRunState,
    onBack: () -> Unit,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onTimeTick: (Int) -> Unit,
    onExit: () -> Unit
) {
    val q = state.questions.getOrNull(state.currentIndex)
    var timeLeft by remember(state.currentIndex, state.questions) { mutableIntStateOf(state.timeLeftSeconds) }

    LaunchedEffect(state.currentIndex, state.showExplanation) {
        if (!state.showExplanation && state.quiz != null) {
            timeLeft = state.quiz.timePerQuestionSeconds
            while (timeLeft > 0 && !state.showExplanation) {
                delay(1000)
                timeLeft -= 1
                onTimeTick(timeLeft)
            }
        }
    }

    // Finished screen
    if (state.isFinished) {
        val ratio = if (state.questions.isEmpty()) 0f else state.correctCount.toFloat() / state.questions.size
        val emoji = when {
            ratio >= 0.9f -> "Отлично!"
            ratio >= 0.7f -> "Хороший результат!"
            ratio >= 0.5f -> "Неплохо!"
            else -> "Есть куда расти!"
        }
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                if (ratio >= 0.7f) Icons.Default.EmojiEvents else Icons.Default.School,
                null,
                modifier = Modifier.size(80.dp),
                tint = if (ratio >= 0.7f) XpGold else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Викторина завершена!", style = MaterialTheme.typography.headlineSmall)
            Text(emoji, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))

            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.correctCount} / ${state.questions.size}", style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("правильных ответов", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (ratio >= 0.7f) CorrectGreen else WrongRed,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("${(ratio * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onExit, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Home, null)
                Spacer(Modifier.width(8.dp))
                Text("На главную")
            }
        }
        return
    }

    if (state.error != null) {
        Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onExit) { Text("Назад") }
        }
        return
    }

    if (q == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val progress by animateFloatAsState(
        targetValue = (state.currentIndex + 1).toFloat() / state.questions.size,
        animationSpec = tween(500), label = "progress"
    )
    val timerProgress by animateFloatAsState(
        targetValue = if (state.quiz != null && state.quiz.timePerQuestionSeconds > 0) timeLeft.toFloat() / state.quiz.timePerQuestionSeconds else 1f,
        animationSpec = tween(300), label = "timer"
    )
    val timerColor = when {
        timeLeft <= 5 -> WrongRed
        timeLeft <= 15 -> StreakOrange
        else -> MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.quiz?.title ?: "Викторина", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Выход") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
        ) {
            // Progress bar + timer
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${state.currentIndex + 1}/${state.questions.size}", style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                if (!state.showExplanation) {
                    Surface(color = timerColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                        Text("${timeLeft}с", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = timerColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            if (!state.showExplanation) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { timerProgress },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = timerColor,
                    trackColor = timerColor.copy(alpha = 0.1f)
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(q.text, style = MaterialTheme.typography.titleLarge)

            // Code snippet
            q.codeSnippet?.let { code ->
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = code,
                        modifier = Modifier.padding(12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Explanation
            AnimatedVisibility(visible = state.showExplanation, enter = fadeIn() + expandVertically()) {
                val correct = state.selectedAnswer != null && q.isCorrect(state.selectedAnswer!!)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (correct) CorrectGreen.copy(alpha = 0.12f) else WrongRed.copy(alpha = 0.12f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (correct) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                null,
                                tint = if (correct) CorrectGreen else WrongRed,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (correct) "Верно!" else "Неверно",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (correct) CorrectGreen else WrongRed
                            )
                        }
                        if (!correct) {
                            Spacer(Modifier.height(8.dp))
                            Text("Правильный ответ: ${q.options.getOrNull(q.correctIndex)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(q.explanation, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNext, modifier = Modifier.align(Alignment.End), shape = RoundedCornerShape(10.dp)) {
                            Text(if (state.currentIndex + 1 < state.questions.size) "Далее" else "Завершить")
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Answer options
            if (!state.showExplanation) {
                q.options.forEachIndexed { index, option ->
                    val letter = ('A' + index).toString()
                    OutlinedCard(
                        onClick = { onSelectAnswer(index) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(32.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(letter, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(option, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}
