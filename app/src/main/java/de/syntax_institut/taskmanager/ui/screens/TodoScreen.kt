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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.syntax_institut.taskmanager.R
import de.syntax_institut.taskmanager.data.model.Task
import de.syntax_institut.taskmanager.ui.components.AddEditTaskDialog
import de.syntax_institut.taskmanager.ui.components.FilterDialog
import de.syntax_institut.taskmanager.ui.components.StatisticItem
import de.syntax_institut.taskmanager.ui.components.TaskItem
import de.syntax_institut.taskmanager.ui.viewmodel.TodoViewModel

@Composable
fun TodoScreen(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = viewModel()
) {
    val showOnlyPending by viewModel.showOnlyPendingStateFlow.collectAsState()
    val filterCategory by viewModel.filterCategoryStateFlow.collectAsState()
    val filterUserId by viewModel.filterUserIdStateFlow.collectAsState()
    val displayedTasks by viewModel.displayedTasks.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val statistics by viewModel.taskStatistics.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()
    val todayTasks by viewModel.todayTasks.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Aufgaben-Übersicht",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        currentUser?.let { user ->
                            Text(
                                text = "Benutzer: ${user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (statistics.total > 0) {
                        Text(
                            text = "${(statistics.completionRate * 100).toInt()}% erledigt",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (statistics.total > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { statistics.completionRate },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem("Gesamt", statistics.total.toString())
                    StatisticItem("Offen", statistics.pending.toString())
                    StatisticItem("Erledigt", statistics.completed.toString())
                }

                if (statistics.overdue > 0 || statistics.dueToday > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (statistics.overdue > 0) {
                            StatisticItem("⚠️ Überfällig", statistics.overdue.toString())
                        }
                        if (statistics.dueToday > 0) {
                            StatisticItem("📅 Heute", statistics.dueToday.toString())
                        }
                        if (statistics.highPriority > 0) {
                            StatisticItem("🔴 Wichtig", statistics.highPriority.toString())
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (overdueTasks.isNotEmpty() || todayTasks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (overdueTasks.isNotEmpty()) {
                    FilterChip(
                        onClick = { /* TODO: Filter auf überfällige Tasks */ },
                        label = { Text("⚠️ ${overdueTasks.size} überfällig") },
                        selected = false
                    )
                }
                if (todayTasks.isNotEmpty()) {
                    FilterChip(
                        onClick = { /* TODO: Filter auf heute fällige Tasks */ },
                        label = { Text("📅 ${todayTasks.size} heute") },
                        selected = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                showOnlyPending -> "Nur offene Aufgaben"
                                filterCategory.isNotEmpty() -> "Kategorie: $filterCategory"
                                filterUserId != -1L -> "Benutzer: ${allUsers.find { it.id == filterUserId }?.username ?: "Unbekannt"}"
                                else -> "Alle Aufgaben"
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "${displayedTasks.size} Aufgaben",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val activeFiltersCount = listOf(
                            showOnlyPending,
                            filterCategory.isNotEmpty(),
                            filterUserId != -1L
                        ).count { it }

                        BadgedBox(
                            badge = {
                                if (activeFiltersCount > 0) {
                                    Badge {
                                        Text(activeFiltersCount.toString())
                                    }
                                }
                            }
                        ) {
                            OutlinedButton(
                                onClick = { showFilterDialog = true }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.filter_list),
                                    contentDescription = "Filter"
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Filter")
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showOnlyPending) "Nur offene" else "Alle",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = showOnlyPending,
                                onCheckedChange = { viewModel.toggleShowOnlyPending() }
                            )
                        }
                    }
                }

                if (filterCategory.isNotEmpty() || filterUserId != -1L) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filterCategory.isNotEmpty()) {
                            FilterChip(
                                onClick = { viewModel.setFilterCategory("") },
                                label = { Text("📁 $filterCategory") },
                                selected = true
                            )
                        }
                        if (filterUserId != -1L) {
                            val userName = allUsers.find { it.id == filterUserId }?.username ?: "Unbekannt"
                            FilterChip(
                                onClick = { viewModel.setFilterUserId(-1L) },
                                label = { Text("👤 $userName") },
                                selected = true
                            )
                        }
                    }
                }

                if (filterCategory.isNotEmpty() || filterUserId != -1L || showOnlyPending) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.clearFilters() }
                    ) {
                        Text("Alle Filter zurücksetzen")
                    }
                }
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
                            text = when {
                                showOnlyPending -> "🎉 Keine offenen Aufgaben!"
                                filterCategory.isNotEmpty() -> "📁 Keine Aufgaben in dieser Kategorie"
                                filterUserId != -1L -> "👤 Keine Aufgaben für diesen Benutzer"
                                else -> "📝 Keine Aufgaben vorhanden"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when {
                                showOnlyPending -> "Alle Aufgaben sind erledigt - super gemacht!"
                                filterCategory.isNotEmpty() -> "Erstelle eine Aufgabe in dieser Kategorie!"
                                filterUserId != -1L -> "Erstelle eine Aufgabe für diesen Benutzer!"
                                else -> "Erstelle deine erste Aufgabe!"
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
                            val assignedUser = allUsers.find { it.id == task.userId }
                            TaskItem(
                                task = task,
                                assignedUser = assignedUser,
                                currentUserId = viewModel.currentUserIdStateFlow.collectAsState().value,
                                onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                                onDelete = { viewModel.deleteTask(task) },
                                onEdit = { editingTask = task }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditTaskDialog(
            categories = categories,
            users = allUsers,
            currentUserId = viewModel.currentUserIdStateFlow.collectAsState().value,
            onDismiss = { showAddDialog = false },
            onConfirm = { task ->
                viewModel.insertTask(task)
                showAddDialog = false
            }
        )
    }

    editingTask?.let { task ->
        AddEditTaskDialog(
            task = task,
            categories = categories,
            users = allUsers,
            currentUserId = viewModel.currentUserIdStateFlow.collectAsState().value,
            onDismiss = { editingTask = null },
            onConfirm = { updatedTask ->
                viewModel.updateTask(updatedTask)
                editingTask = null
            }
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            showOnlyPending = showOnlyPending,
            filterCategory = filterCategory,
            filterUserId = filterUserId,
            categories = categories,
            users = allUsers,
            onDismiss = { showFilterDialog = false },
            onShowOnlyPendingChange = { viewModel.toggleShowOnlyPending() },
            onFilterCategoryChange = { viewModel.setFilterCategory(it) },
            onFilterUserIdChange = { viewModel.setFilterUserId(it) },
            onClearFilters = { viewModel.clearFilters() }
        )
    }
}