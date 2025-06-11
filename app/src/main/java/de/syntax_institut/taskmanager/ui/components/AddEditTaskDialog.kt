package de.syntax_institut.taskmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import de.syntax_institut.taskmanager.data.model.Task
import de.syntax_institut.taskmanager.data.model.TaskPriority
import de.syntax_institut.taskmanager.utils.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    task: Task? = null,
    categories: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var category by remember { mutableStateOf(task?.category ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var hasDeadline by remember { mutableStateOf(task?.deadlineTimestamp != null) }
    var deadlineTimestamp by remember { mutableStateOf(task?.deadlineTimestamp) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = deadlineTimestamp ?: System.currentTimeMillis()
    )
    
    val timePickerState = rememberTimePickerState(
        initialHour = if (deadlineTimestamp != null) {
            Calendar.getInstance().apply { timeInMillis = deadlineTimestamp!! }.get(Calendar.HOUR_OF_DAY)
        } else 23,
        initialMinute = if (deadlineTimestamp != null) {
            Calendar.getInstance().apply { timeInMillis = deadlineTimestamp!! }.get(Calendar.MINUTE)
        } else 59
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (task == null) "Neue Aufgabe erstellen" else "Aufgabe bearbeiten") 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel *") },
                    placeholder = { Text("z.B. Einkaufen gehen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    placeholder = { Text("Weitere Details zur Aufgabe...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
                ) {
                    TextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategorie") },
                        placeholder = { Text("z.B. Privat, Arbeit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = showPriorityDropdown,
                    onExpandedChange = { showPriorityDropdown = !showPriorityDropdown }
                ) {
                    TextField(
                        value = when (priority) {
                            TaskPriority.LOW -> "🟢 Niedrig"
                            TaskPriority.MEDIUM -> "🟡 Normal"
                            TaskPriority.HIGH -> "🟠 Hoch"
                            TaskPriority.URGENT -> "🔴 Dringend"
                        },
                        onValueChange = { },
                        label = { Text("Priorität") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPriorityDropdown) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showPriorityDropdown,
                        onDismissRequest = { showPriorityDropdown = false }
                    ) {
                        TaskPriority.entries.forEach { prio ->
                            DropdownMenuItem(
                                text = { 
                                    Text(when (prio) {
                                        TaskPriority.LOW -> "🟢 Niedrig"
                                        TaskPriority.MEDIUM -> "🟡 Normal"
                                        TaskPriority.HIGH -> "🟠 Hoch"
                                        TaskPriority.URGENT -> "🔴 Dringend"
                                    })
                                },
                                onClick = {
                                    priority = prio
                                    showPriorityDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Deadline setzen",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = hasDeadline,
                        onCheckedChange = { 
                            hasDeadline = it
                            if (!it) deadlineTimestamp = null
                        }
                    )
                }
                
                if (hasDeadline) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = { showDatePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        deadlineTimestamp?.let { DateUtils.formatDate(it) } ?: "Datum"
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Button(
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        deadlineTimestamp?.let { DateUtils.formatTime(it) } ?: "Zeit"
                                    )
                                }
                            }
                            
                            if (deadlineTimestamp != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Deadline: ${DateUtils.formatDateTime(deadlineTimestamp!!)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTask = task?.copy(
                        title = title.trim(),
                        description = description.trim(),
                        category = category.trim(),
                        priority = priority,
                        deadlineTimestamp = deadlineTimestamp
                    )
                        ?: Task(
                            title = title.trim(),
                            description = description.trim(),
                            category = category.trim(),
                            priority = priority,
                            deadlineTimestamp = deadlineTimestamp
                        )
                    onConfirm(newTask)
                    keyboardController?.hide()
                },
                enabled = title.isNotBlank()
            ) {
                Text(if (task == null) "Hinzufügen" else "Speichern")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    keyboardController?.hide()
                }
            ) {
                Text("Abbrechen")
            }
        }
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            val calendar = Calendar.getInstance()
                            if (deadlineTimestamp != null) {
                                calendar.timeInMillis = deadlineTimestamp!!
                            }
                            
                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.timeInMillis = selectedDate
                            
                            calendar.set(
                                selectedCalendar.get(Calendar.YEAR),
                                selectedCalendar.get(Calendar.MONTH),
                                selectedCalendar.get(Calendar.DAY_OF_MONTH)
                            )
                            
                            deadlineTimestamp = calendar.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        if (deadlineTimestamp != null) {
                            calendar.timeInMillis = deadlineTimestamp!!
                        } else {
                            calendar.timeInMillis = System.currentTimeMillis()
                        }
                        
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        
                        deadlineTimestamp = calendar.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

