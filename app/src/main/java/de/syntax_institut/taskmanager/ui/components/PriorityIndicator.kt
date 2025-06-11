package de.syntax_institut.taskmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.syntax_institut.taskmanager.data.model.TaskPriority

@Composable
fun PriorityIndicator(priority: TaskPriority) {
    val (color, icon) = when (priority) {
        TaskPriority.LOW -> Color(0xFF4CAF50) to "🟢"
        TaskPriority.MEDIUM -> Color(0xFFFFEB3B) to "🟡"
        TaskPriority.HIGH -> Color(0xFFFF9800) to "🟠"
        TaskPriority.URGENT -> Color(0xFFF44336) to "🔴"
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodySmall
        )
    }
}