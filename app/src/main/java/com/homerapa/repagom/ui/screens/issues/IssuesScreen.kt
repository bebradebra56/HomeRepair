package com.homerapa.repagom.ui.screens.issues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homerapa.repagom.data.db.IssueEntity
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.IssueFilter
import com.homerapa.repagom.viewmodel.IssueViewModel

@Composable
fun IssuesScreen(
    onNavigateToIssue: (Long) -> Unit,
    onNavigateToCreateIssue: () -> Unit,
    viewModel: IssueViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedPriority by remember { mutableStateOf<String?>(null) }
    var showOnlyFixed by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<IssueEntity?>(null) }

    val statuses = listOf("New", "In Review", "In Progress", "Waiting", "Fixed", "Closed")
    val priorities = listOf("Critical", "High", "Medium", "Low")

    val displayedIssues = uiState.filteredIssues.filter { issue ->
        issue.title.contains(searchQuery, ignoreCase = true) ||
                issue.description.contains(searchQuery, ignoreCase = true) ||
                issue.category.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(selectedStatus, selectedPriority, showOnlyFixed) {
        viewModel.setFilter(
            IssueFilter(
                status = selectedStatus,
                priority = selectedPriority,
                onlyFixed = showOnlyFixed
            )
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateIssue,
                containerColor = WarmRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Issue")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBlue)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text("Issues", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        "${displayedIssues.count { it.status !in listOf("Fixed","Closed") }} open  ·  ${displayedIssues.count { it.status in listOf("Fixed","Closed") }} fixed",
                        style = MaterialTheme.typography.bodySmall, color = SkyBlue.copy(alpha = 0.7f)
                    )
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Search issues...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatus == null && selectedPriority == null && !showOnlyFixed,
                        onClick = { selectedStatus = null; selectedPriority = null; showOnlyFixed = false },
                        label = { Text("All") }
                    )
                }
                items(statuses) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = if (selectedStatus == status) null else status },
                        label = { Text(status) }
                    )
                }
                items(priorities) { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = if (selectedPriority == priority) null else priority },
                        label = { Text(priority) }
                    )
                }
            }

            if (displayedIssues.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.ReportProblem,
                        title = "No Issues Found",
                        subtitle = if (searchQuery.isEmpty() && selectedStatus == null) "Tap + to log your first issue" else "Try adjusting your filters"
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedIssues, key = { it.id }) { issue ->
                        IssueCard(
                            issue = issue,
                            onClick = { onNavigateToIssue(issue.id) },
                            onDelete = { showDeleteDialog = issue },
                            onMarkFixed = { viewModel.markFixed(issue) }
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    showDeleteDialog?.let { issue ->
        ConfirmDeleteDialog(
            title = "Delete Issue",
            message = "Delete \"${issue.title}\"? This cannot be undone.",
            onConfirm = { viewModel.deleteIssue(issue); showDeleteDialog = null },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
fun IssueCard(
    issue: IssueEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMarkFixed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isFixed = issue.status in listOf("Fixed", "Closed")

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFixed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isFixed) 1.dp else 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryIcon(issue.category, modifier = Modifier.size(16.dp))
                        Text(
                            issue.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryText
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        issue.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFixed) SecondaryText else DarkText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (issue.description.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            issue.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        if (!isFixed) {
                            DropdownMenuItem(
                                text = { Text("Mark as Fixed") },
                                onClick = { menuExpanded = false; onMarkFixed() },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete", color = WarmRed) },
                            onClick = { menuExpanded = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = WarmRed) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityChip(issue.priority)
                StatusChip(issue.status)
                Spacer(Modifier.weight(1f))
                if (issue.deadline != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = SecondaryText.copy(alpha = 0.6f), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(issue.deadline.toFormattedDate(), style = MaterialTheme.typography.labelSmall, color = SecondaryText.copy(alpha = 0.7f))
                    }
                }
                if (issue.assignedTo.isNotEmpty()) {
                    AvatarCircle(issue.assignedTo, size = 24.dp, backgroundColor = NavyBlue.copy(alpha = 0.7f))
                }
            }
        }
    }
}
