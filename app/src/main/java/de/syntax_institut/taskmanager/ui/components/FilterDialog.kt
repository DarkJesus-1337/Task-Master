package de.syntax_institut.taskmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.syntax_institut.taskmanager.data.model.User

@Composable
fun FilterDialog(
    showOnlyPending: Boolean,
    filterCategory: String,
    filterUserId: Long,
    categories: List<String>,
    users: List<User>,
    onDismiss: () -> Unit,
    onShowOnlyPendingChange: (Boolean) -> Unit,
    onFilterCategoryChange: (String) -> Unit,
    onFilterUserIdChange: (Long) -> Unit,
    onClearFilters: () -> Unit
) {
    var tempShowOnlyPending by remember { mutableStateOf(showOnlyPending) }
    var tempFilterCategory by remember { mutableStateOf(filterCategory) }
    var tempFilterUserId by remember { mutableLongStateOf(filterUserId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = "Filter & Sortierung",
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📋 Status",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nur offene Aufgaben anzeigen",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = tempShowOnlyPending,
                                onCheckedChange = { tempShowOnlyPending = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (users.size > 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "👤 Benutzer",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    onClick = { tempFilterUserId = -1L },
                                    label = { Text("Alle Benutzer") },
                                    selected = tempFilterUserId == -1L
                                )
                                
                                users.forEach { user ->
                                    FilterChip(
                                        onClick = {
                                            tempFilterUserId = if (tempFilterUserId == user.id) -1L else user.id
                                        },
                                        label = { 
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(user.username)
                                            }
                                        },
                                        selected = tempFilterUserId == user.id
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (categories.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📁 Kategorien",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    onClick = { tempFilterCategory = "" },
                                    label = { Text("Alle Kategorien") },
                                    selected = tempFilterCategory.isEmpty()
                                )
                                
                                categories.forEach { category ->
                                    FilterChip(
                                        onClick = {
                                            tempFilterCategory = if (tempFilterCategory == category) "" else category
                                        },
                                        label = { Text("📁 $category") },
                                        selected = tempFilterCategory == category
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (tempShowOnlyPending || tempFilterCategory.isNotEmpty() || tempFilterUserId != -1L) {
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
                                text = "🔍 Aktive Filter:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            val activeFilters = mutableListOf<String>()
                            if (tempShowOnlyPending) activeFilters.add("Nur offene Aufgaben")
                            if (tempFilterCategory.isNotEmpty()) activeFilters.add("Kategorie: $tempFilterCategory")
                            if (tempFilterUserId != -1L) {
                                val userName = users.find { it.id == tempFilterUserId }?.username ?: "Unbekannt"
                                activeFilters.add("Benutzer: $userName")
                            }
                            
                            activeFilters.forEach { filter ->
                                Text(
                                    text = "• $filter",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onShowOnlyPendingChange(tempShowOnlyPending)
                    onFilterCategoryChange(tempFilterCategory)
                    onFilterUserIdChange(tempFilterUserId)
                    onDismiss()
                }
            ) {
                Text("Anwenden")
            }
        },
        dismissButton = {
            Row {
                if (tempShowOnlyPending || tempFilterCategory.isNotEmpty() || tempFilterUserId != -1L) {
                    TextButton(
                        onClick = {
                            tempShowOnlyPending = false
                            tempFilterCategory = ""
                            tempFilterUserId = -1L
                            onClearFilters()
                            onDismiss()
                        }
                    ) {
                        Text("Zurücksetzen")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }
            }
        }
    )
}