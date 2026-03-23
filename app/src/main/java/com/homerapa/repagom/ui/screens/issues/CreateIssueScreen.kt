package com.homerapa.repagom.ui.screens.issues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.IssueViewModel
import com.homerapa.repagom.viewmodel.ProjectViewModel

private val issueCategories = listOf("Crack", "Paint", "Tile", "Electric", "Plumbing", "Furniture", "Finish", "Delivery", "Other")
private val issuePriorities = listOf("Low", "Medium", "High", "Critical")
private val issueStatuses = listOf("New", "In Review", "In Progress", "Waiting", "Fixed", "Closed")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIssueScreen(
    projectId: Long?,
    roomId: Long?,
    photoId: Long?,
    onBack: () -> Unit,
    onIssueCreated: () -> Unit,
    issueViewModel: IssueViewModel = viewModel(),
    projectViewModel: ProjectViewModel = viewModel()
) {
    val projectState by projectViewModel.uiState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }
    var priority by remember { mutableStateOf("Medium") }
    var status by remember { mutableStateOf("New") }
    var assignedTo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var projectError by remember { mutableStateOf(false) }
    var selectedProjectId by remember { mutableStateOf(projectId) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var projectMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "New Issue", onBack = onBack) },
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
                label = { Text("Issue Title *") },
                isError = titleError,
                supportingText = if (titleError) {{ Text("Required") }} else null,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.ReportProblem, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            // Project selection (if not pre-set)
            if (projectId == null) {
                ExposedDropdownMenuBox(
                    expanded = projectMenuExpanded,
                    onExpandedChange = { projectMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = projectState.projects.find { it.id == selectedProjectId }?.name ?: "Select Project *",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Project *") },
                        isError = projectError,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
                    )
                    ExposedDropdownMenu(expanded = projectMenuExpanded, onDismissRequest = { projectMenuExpanded = false }) {
                        projectState.projects.forEach { project ->
                            DropdownMenuItem(
                                text = { Text(project.name) },
                                onClick = { selectedProjectId = project.id; projectMenuExpanded = false; projectError = false }
                            )
                        }
                    }
                }
            }

            // Category
            ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { CategoryIcon(category, modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                    issueCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = { category = cat; categoryMenuExpanded = false },
                            leadingIcon = { CategoryIcon(cat, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Priority selector
            Text("Priority", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = DarkText)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                issuePriorities.forEach { p ->
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
                            p,
                            color = if (selected) Color.White else SecondaryText,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Status selector
            ExposedDropdownMenuBox(expanded = statusMenuExpanded, onExpandedChange = { statusMenuExpanded = it }) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { StatusChip(status) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                    issueStatuses.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = { status = s; statusMenuExpanded = false }
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
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            if (photoId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SkyBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Linked to photo #$photoId", style = MaterialTheme.typography.bodySmall, color = NavyBlue)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) { titleError = true; return@Button }
                    val pid = selectedProjectId
                    if (pid == null) { projectError = true; return@Button }
                    issueViewModel.createIssue(
                        projectId = pid,
                        roomId = roomId,
                        photoId = photoId,
                        title = title,
                        description = description,
                        category = category,
                        priority = priority,
                        status = status,
                        assignedTo = assignedTo,
                        deadline = null,
                        markerX = null,
                        markerY = null,
                        notes = notes
                    )
                    onIssueCreated()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarmRed)
            ) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create Issue", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
