package com.homerapa.repagom.ui.screens.tasks

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homerapa.repagom.ui.components.AppTopBar
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.ProjectViewModel
import com.homerapa.repagom.viewmodel.TaskViewModel

private val taskPriorities = listOf("Low", "Medium", "High", "Critical")
private val taskStatuses = listOf("Todo", "In Progress", "Done")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    projectId: Long?,
    roomId: Long?,
    issueId: Long?,
    onBack: () -> Unit,
    onTaskCreated: () -> Unit,
    taskViewModel: TaskViewModel = viewModel(),
    projectViewModel: ProjectViewModel = viewModel()
) {
    val projectState by projectViewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var assignedTo by remember { mutableStateOf("") }
    var costEstimate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var selectedProjectId by remember { mutableStateOf(projectId) }
    var projectMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "New Task", onBack = onBack) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Task Title *") },
                isError = titleError,
                supportingText = if (titleError) {{ Text("Required") }} else null,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Task, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            if (projectId == null) {
                ExposedDropdownMenuBox(expanded = projectMenuExpanded, onExpandedChange = { projectMenuExpanded = it }) {
                    OutlinedTextField(
                        value = projectState.projects.find { it.id == selectedProjectId }?.name ?: "Select Project",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Project") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = projectMenuExpanded, onDismissRequest = { projectMenuExpanded = false }) {
                        projectState.projects.forEach { project ->
                            DropdownMenuItem(
                                text = { Text(project.name) },
                                onClick = { selectedProjectId = project.id; projectMenuExpanded = false }
                            )
                        }
                    }
                }
            }

            // Priority
            Text("Priority", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = DarkText)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                taskPriorities.forEach { p ->
                    val selected = priority == p
                    val color = when (p) {
                        "Critical" -> PriorityCritical
                        "High" -> PriorityHigh
                        "Medium" -> PriorityMedium
                        else -> PriorityLow
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) color else Color.Transparent,
                        modifier = Modifier
                            .border(1.dp, if (selected) color else DividerColor, RoundedCornerShape(20.dp))
                            .clickable { priority = p }
                    ) {
                        Text(
                            p, color = if (selected) Color.White else SecondaryText,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = assignedTo,
                onValueChange = { assignedTo = it },
                label = { Text("Assigned To") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = costEstimate,
                onValueChange = { costEstimate = it },
                label = { Text("Cost Estimate ($)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            if (issueId != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StatusOrange.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = StatusOrange, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Linked to issue #$issueId", style = MaterialTheme.typography.bodySmall, color = StatusOrange)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    val pid = selectedProjectId ?: projectState.projects.firstOrNull()?.id ?: return@Button
                    taskViewModel.createTask(
                        projectId = pid,
                        roomId = roomId,
                        issueId = issueId,
                        title = title,
                        description = description,
                        deadline = null,
                        priority = priority,
                        assignedTo = assignedTo,
                        costEstimate = costEstimate.toDoubleOrNull() ?: 0.0,
                        notes = notes
                    )
                    onTaskCreated()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Task", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
