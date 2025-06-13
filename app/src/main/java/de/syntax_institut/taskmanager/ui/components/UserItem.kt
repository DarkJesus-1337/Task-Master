package de.syntax_institut.taskmanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun UserItem(
    userWithTasks: de.syntax_institut.taskmanager.data.model.UserWithTasks,
    isCurrentUser: Boolean,
    onSelectUser: () -> Unit,
    onDeleteUser: () -> Unit,
    canDelete: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isCurrentUser,
                onClick = onSelectUser
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userWithTasks.user.username,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isCurrentUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                val completedTasks = userWithTasks.tasks.count { it.isCompleted }
                val totalTasks = userWithTasks.tasks.size
                val pendingTasks = totalTasks - completedTasks

                Text(
                    text = "$totalTasks Aufgaben • $completedTasks erledigt • $pendingTasks offen",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentUser) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }
                )

                if (isCurrentUser) {
                    Text(
                        text = "🟢 Aktiver Benutzer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (canDelete) {
                IconButton(
                    onClick = onDeleteUser,
                    enabled = !isCurrentUser
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Benutzer löschen",
                        tint = if (isCurrentUser) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }
    }
}