package com.example.myquizbite.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myquizbite.data.model.Difficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    onBack: () -> Unit,
    onStartInterview: (Difficulty) -> Unit,
    onStartAdaptive: () -> Unit
) {
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var useAdaptive by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Режим «Собеседование»") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useAdaptive, onCheckedChange = { useAdaptive = it })
                Column {
                    Text("Адаптивная сложность", style = MaterialTheme.typography.titleSmall)
                    Text("Вопросы подбираются по твоей точности", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(16.dp))
            if (!useAdaptive) {
                Text("Выбери сложность вручную:", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Difficulty.entries.forEach { d ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedDifficulty == d, onClick = { selectedDifficulty = d })
                        Spacer(Modifier.width(8.dp))
                        Text(d.displayName)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (useAdaptive) onStartAdaptive() else onStartInterview(selectedDifficulty)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Начать собеседование")
            }
        }
    }
}
