package de.syntax_institut.taskmanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.syntax_institut.taskmanager.ui.viewmodel.TodoViewModel

@Composable
fun TodoScreen(
    viewModel: TodoViewModel = viewModel()
) {
    val showCompleted by viewModel.showCompletedStateFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showCompleted) "Zeige: Erledigte ToDos" else "Zeige: Alle ToDos",
                style = MaterialTheme.typography.headlineSmall
            )

            Switch(
                checked = showCompleted,
                onCheckedChange = { viewModel.toggleShowCompleted() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ToDo-Liste",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (showCompleted) {
                        "Hier werden später nur die erledigten ToDos angezeigt"
                    } else {
                        "Hier werden später alle ToDos angezeigt"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}