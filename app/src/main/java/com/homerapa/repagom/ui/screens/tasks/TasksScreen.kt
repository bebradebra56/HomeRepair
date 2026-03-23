package com.homerapa.repagom.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homerapa.repagom.data.db.TaskEntity
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.TaskViewModel

@Composable
fun TasksScreen(
    projectId: Long,
    onBack: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    viewModel: TaskViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<TaskEntity?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(projectId) {
        viewModel.loadTasksForProject(projectId)
    }

    val filteredTasks = when (selectedFilter) {
        "Todo" -> uiState.tasks.filter { it.status == "Todo" }
        "In Progress" -> uiState.tasks.filter { it.status == "In Progress" }
        "Done" -> uiState.tasks.filter { it.status == "Done" }
        else -> uiState.tasks
    }

    Scaffold(
        topBar = { AppTopBar(title = "Tasks", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateTask,
                containerColor = StatusOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = uiState.tasks.count { it.status == "Todo" }.toString(),
                    label = "Todo",
                    icon = Icons.Default.RadioButtonUnchecked,
                    backgroundColor = StatusBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.tasks.count { it.status == "In Progress" }.toString(),
                    label = "In Progress",
                    icon = Icons.Default.Timelapse,
                    backgroundColor = StatusOrange,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.tasks.count { it.status == "Done" }.toString(),
                    label = "Done",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = StatusGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Filters
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Todo", "In Progress", "Done").forEach { f ->
                    FilterChip(
                        selected = selectedFilter == f,
                        onClick = { selectedFilter = f },
                        label = { Text(f) }
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.Task,
                        title = "No Tasks",
                        subtitle = "Tap + to add a task"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onStatusChange = { newStatus -> viewModel.updateTaskStatus(task, newStatus) },
                            onDelete = { showDeleteDialog = task }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    showDeleteDialog?.let { task ->
        ConfirmDeleteDialog(
            title = "Delete Task",
            message = "Delete \"${task.title}\"?",
            onConfirm = { viewModel.deleteTask(task); showDeleteDialog = null },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == "Done"
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isDone) 1.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            IconButton(
                onClick = {
                    when (task.status) {
                        "Todo" -> onStatusChange("In Progress")
                        "In Progress" -> onStatusChange("Done")
                        "Done" -> onStatusChange("Todo")
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    when (task.status) {
                        "Done" -> Icons.Default.CheckCircle
                        "In Progress" -> Icons.Default.Timelapse
                        else -> Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = when (task.status) {
                        "Done" -> StatusGreen
                        "In Progress" -> StatusOrange
                        else -> SecondaryText.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDone) SecondaryText else DarkText,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotEmpty()) {
                    Text(task.description, style = MaterialTheme.typography.bodySmall, color = SecondaryText.copy(alpha = 0.8f))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    StatusChip(task.status)
                    PriorityChip(task.priority)
                    if (task.deadline != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = SecondaryText.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(task.deadline.toFormattedDate(), style = MaterialTheme.typography.labelSmall, color = SecondaryText.copy(alpha = 0.7f))
                        }
                    }
                }
                if (task.costEstimate > 0.0) {
                    Text(
                        "Est. $${task.costEstimate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmBrown,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    listOf("Todo", "In Progress", "Done").forEach { s ->
                        if (s != task.status) {
                            DropdownMenuItem(
                                text = { Text("Set: $s") },
                                onClick = { menuExpanded = false; onStatusChange(s) }
                            )
                        }
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = WarmRed) },
                        onClick = { menuExpanded = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = WarmRed) }
                    )
                }
            }
        }
    }
}
