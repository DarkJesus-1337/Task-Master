package de.syntax_institut.taskmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.syntax_institut.taskmanager.ui.components.StatisticItem
import de.syntax_institut.taskmanager.ui.components.TaskItem
import de.syntax_institut.taskmanager.ui.viewmodel.TodoViewModel

@Composable
fun TodoScreen(
    viewModel: TodoViewModel = viewModel()
) {
    val showCompleted by viewModel.showCompletedStateFlow.collectAsState()
    val displayedTasks by viewModel.displayedTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()

    var newTaskTitle by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val totalTasks = allTasks.size
    val completedCount = allTasks.count { it.isCompleted }
    val pendingCount = totalTasks - completedCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Aufgaben-Übersicht",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem("Gesamt", totalTasks.toString())
                    StatisticItem("Offen", pendingCount.toString())
                    StatisticItem("Erledigt", completedCount.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (showCompleted) "Erledigte Aufgaben" else "Alle Aufgaben",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "${displayedTasks.size} Aufgaben",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showCompleted) "Nur erledigte" else "Alle",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = showCompleted,
                    onCheckedChange = { viewModel.toggleShowCompleted() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Task hinzufügen")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Neue Aufgabe hinzufügen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxHeight()
            ) {
                if (displayedTasks.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (showCompleted) {
                                "🎉 Noch keine erledigten Aufgaben"
                            } else {
                                "📝 Keine Aufgaben vorhanden"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (showCompleted) {
                                "Erledige eine Aufgabe, um sie hier zu sehen!"
                            } else {
                                "Erstelle deine erste Aufgabe!"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedTasks, key = { it.id }) { task ->
                            TaskItem(
                                task = task,
                                onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                                onDelete = { viewModel.deleteTask(task) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newTaskTitle = ""
            },
            title = { Text("Neue Aufgabe erstellen") },
            text = {
                Column {
                    Text(
                        text = "Gib einen Titel für deine neue Aufgabe ein:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Aufgabentitel") },
                        placeholder = { Text("z.B. Einkaufen gehen") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.insertTask(newTaskTitle.trim())
                            showAddDialog = false
                            newTaskTitle = ""
                            keyboardController?.hide()
                        }
                    },
                    enabled = newTaskTitle.isNotBlank()
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        newTaskTitle = ""
                        keyboardController?.hide()
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}


