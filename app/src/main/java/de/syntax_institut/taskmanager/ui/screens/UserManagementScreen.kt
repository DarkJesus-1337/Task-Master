package de.syntax_institut.taskmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.syntax_institut.taskmanager.data.model.User
import de.syntax_institut.taskmanager.ui.components.AddUserDialog
import de.syntax_institut.taskmanager.ui.components.StatisticItem
import de.syntax_institut.taskmanager.ui.components.UserItem
import de.syntax_institut.taskmanager.ui.viewmodel.UserManagementViewModel

@Composable
fun UserManagementScreen(
    modifier: Modifier = Modifier,
    viewModel: UserManagementViewModel = viewModel()
) {
    val usersWithTasks by viewModel.allUsersWithTasks.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var showDeleteUserDialog by remember { mutableStateOf<User?>(null) }

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
                            text = "Benutzer-Verwaltung",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Aktueller Benutzer: ${currentUser?.username ?: "Nicht ausgewählt"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Benutzer",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem("Benutzer", usersWithTasks.size.toString())
                    StatisticItem("Gesamt Aufgaben", usersWithTasks.sumOf { it.tasks.size }.toString())
                    StatisticItem("Erledigt", usersWithTasks.sumOf { it.tasks.count { task -> task.isCompleted } }.toString())
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showAddUserDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Benutzer hinzufügen")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Neuen Benutzer erstellen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Benutzer (${usersWithTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (usersWithTasks.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "👤 Keine Benutzer vorhanden",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Erstellen Sie Ihren ersten Benutzer!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(usersWithTasks, key = { it.user.id }) { userWithTasks ->
                            UserItem(
                                userWithTasks = userWithTasks,
                                isCurrentUser = currentUser?.id == userWithTasks.user.id,
                                onSelectUser = { viewModel.switchToUser(userWithTasks.user.id) },
                                onDeleteUser = {
                                    if (usersWithTasks.size > 1) {
                                        showDeleteUserDialog = userWithTasks.user
                                    }
                                },
                                canDelete = usersWithTasks.size > 1
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { username ->
                viewModel.createUser(username)
                showAddUserDialog = false
            }
        )
    }

    showDeleteUserDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteUserDialog = null },
            title = { Text("Benutzer löschen") },
            text = {
                Text("Möchten Sie den Benutzer \"${user.username}\" wirklich löschen? Alle zugehörigen Aufgaben werden ebenfalls gelöscht.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user)
                        showDeleteUserDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteUserDialog = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

