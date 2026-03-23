package com.homerapa.repagom.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.ReportsViewModel

@Composable
fun ReportsScreen(
    onNavigateToActivityHistory: () -> Unit,
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val categoryGroups = uiState.allIssues
        .groupBy { it.category }
        .mapValues { it.value.size }
        .entries
        .sortedByDescending { it.value }
        .take(6)

    val priorityGroups = uiState.allIssues
        .groupBy { it.priority }
        .mapValues { it.value.size }

    val fixedPercent = if (uiState.totalIssues > 0) {
        (uiState.fixedIssues.toFloat() / uiState.totalIssues * 100).toInt()
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyBlue)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reports", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Project analytics & insights", style = MaterialTheme.typography.bodySmall, color = SkyBlue.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onNavigateToActivityHistory) {
                        Icon(Icons.Default.History, contentDescription = "Activity", tint = Color.White)
                    }
                }
            }
        }

        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = uiState.totalPhotos.toString(),
                    label = "Photos",
                    icon = Icons.Default.Photo,
                    backgroundColor = NavyBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.totalIssues.toString(),
                    label = "Total Issues",
                    icon = Icons.Default.ReportProblem,
                    backgroundColor = WarmBrown,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = uiState.openIssues.toString(),
                    label = "Open Issues",
                    icon = Icons.Default.Warning,
                    backgroundColor = WarmRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.fixedIssues.toString(),
                    label = "Fixed",
                    icon = Icons.Default.CheckCircle,
                    backgroundColor = StatusGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.inProgressTasks.toString(),
                    label = "In Progress",
                    icon = Icons.Default.Engineering,
                    backgroundColor = StatusOrange,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // Fix Progress
        item {
            SectionHeader(title = "Fix Progress")
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Circular progress
                    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(100.dp)) {
                            val stroke = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                            drawArc(
                                color = DividerColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = stroke
                            )
                            drawArc(
                                color = StatusGreen,
                                startAngle = -90f,
                                sweepAngle = 3.6f * fixedPercent,
                                useCenter = false,
                                style = stroke
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$fixedPercent%", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusGreen)
                            Text("Fixed", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProgressLegendItem("Fixed", uiState.fixedIssues, uiState.totalIssues, StatusGreen)
                        ProgressLegendItem("Open", uiState.openIssues, uiState.totalIssues, WarmRed)
                        ProgressLegendItem("In Progress", uiState.inProgressTasks, uiState.totalIssues, StatusOrange)
                    }
                }
            }
        }

        // Issue Categories Chart
        if (categoryGroups.isNotEmpty()) {
            item {
                SectionHeader(title = "Issues by Category")
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val maxVal = categoryGroups.maxOf { it.value }.toFloat()
                        categoryGroups.forEach { (category, count) ->
                            CategoryBarRow(category, count, maxVal)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // Priority breakdown
        if (priorityGroups.isNotEmpty()) {
            item {
                SectionHeader(title = "Priority Breakdown")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Critical" to PriorityCritical, "High" to PriorityHigh, "Medium" to PriorityMedium, "Low" to PriorityLow).forEach { (p, c) ->
                        val count = priorityGroups[p] ?: 0
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = c.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$count", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c)
                                Text(p, style = MaterialTheme.typography.labelSmall, color = c.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ProgressLegendItem(label: String, count: Int, total: Int, color: Color) {
    val percent = if (total > 0) (count.toFloat() / total * 100).toInt() else 0
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = SecondaryText, modifier = Modifier.weight(1f))
        Text("$count ($percent%)", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategoryBarRow(category: String, count: Int, maxVal: Float) {
    val fraction = count / maxVal
    val colors = listOf(WarmRed, StatusOrange, WarmBrown, NavyBlue, StatusGreen, LightWood)
    val color = colors[category.hashCode().and(0x7FFFFFFF) % colors.size]

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(category, style = MaterialTheme.typography.labelSmall, color = SecondaryText, modifier = Modifier.width(72.dp))
        Box(
            modifier = Modifier.weight(1f).height(20.dp).clip(RoundedCornerShape(4.dp)).background(DividerColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Text("$count", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
    }
}
