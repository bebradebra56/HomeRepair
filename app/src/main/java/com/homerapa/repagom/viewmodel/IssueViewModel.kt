package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.db.IssueEntity
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class IssueFilter(
    val roomId: Long? = null,
    val priority: String? = null,
    val status: String? = null,
    val onlyFixed: Boolean = false,
    val onlyWithPhoto: Boolean = false
)

data class IssueUiState(
    val issues: List<IssueEntity> = emptyList(),
    val filteredIssues: List<IssueEntity> = emptyList(),
    val selectedIssue: IssueEntity? = null,
    val filter: IssueFilter = IssueFilter(),
    val isLoading: Boolean = true
)

class IssueViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository

    private val _uiState = MutableStateFlow(IssueUiState())
    val uiState: StateFlow<IssueUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllIssues().collect { issues ->
                val filter = _uiState.value.filter
                _uiState.update { it.copy(issues = issues, filteredIssues = applyFilter(issues, filter), isLoading = false) }
            }
        }
    }

    fun loadIssuesForProject(projectId: Long) = viewModelScope.launch {
        repository.getIssuesForProject(projectId).collect { issues ->
            val filter = _uiState.value.filter
            _uiState.update { it.copy(issues = issues, filteredIssues = applyFilter(issues, filter), isLoading = false) }
        }
    }

    fun loadIssuesForRoom(roomId: Long) = viewModelScope.launch {
        repository.getIssuesForRoom(roomId).collect { issues ->
            val filter = _uiState.value.filter
            _uiState.update { it.copy(issues = issues, filteredIssues = applyFilter(issues, filter), isLoading = false) }
        }
    }

    fun loadIssue(id: Long) = viewModelScope.launch {
        repository.getIssue(id).collect { issue ->
            _uiState.update { it.copy(selectedIssue = issue) }
        }
    }

    fun setFilter(filter: IssueFilter) {
        _uiState.update { state ->
            state.copy(filter = filter, filteredIssues = applyFilter(state.issues, filter))
        }
    }

    fun clearFilter() = setFilter(IssueFilter())

    private fun applyFilter(issues: List<IssueEntity>, filter: IssueFilter): List<IssueEntity> {
        return issues.filter { issue ->
            (filter.roomId == null || issue.roomId == filter.roomId) &&
                    (filter.priority == null || issue.priority == filter.priority) &&
                    (filter.status == null || issue.status == filter.status) &&
                    (!filter.onlyFixed || issue.status == "Fixed" || issue.status == "Closed") &&
                    (!filter.onlyWithPhoto || issue.photoId != null)
        }
    }

    fun createIssue(
        projectId: Long,
        roomId: Long?,
        photoId: Long?,
        title: String,
        description: String,
        category: String,
        priority: String,
        status: String,
        assignedTo: String,
        deadline: Long?,
        markerX: Float?,
        markerY: Float?,
        notes: String
    ) = viewModelScope.launch {
        val issue = IssueEntity(
            projectId = projectId,
            roomId = roomId,
            photoId = photoId,
            title = title,
            description = description,
            category = category,
            priority = priority,
            status = status,
            assignedTo = assignedTo,
            deadline = deadline,
            markerX = markerX,
            markerY = markerY,
            notes = notes
        )
        repository.insertIssue(issue)
        repository.logActivity(projectId, "Issue Created", "Created issue: $title ($priority priority)")
    }

    fun updateIssue(issue: IssueEntity) = viewModelScope.launch {
        repository.updateIssue(issue.copy(updatedAt = System.currentTimeMillis()))
        repository.logActivity(issue.projectId, "Issue Updated", "Updated: ${issue.title}")
    }

    fun markFixed(issue: IssueEntity, afterPhotoUri: String? = null) = viewModelScope.launch {
        repository.updateIssue(
            issue.copy(
                status = "Fixed",
                afterPhotoUri = afterPhotoUri ?: issue.afterPhotoUri,
                updatedAt = System.currentTimeMillis()
            )
        )
        repository.logActivity(issue.projectId, "Issue Fixed", "Fixed: ${issue.title}")
    }

    fun deleteIssue(issue: IssueEntity) = viewModelScope.launch {
        repository.deleteIssue(issue)
        repository.logActivity(issue.projectId, "Issue Deleted", "Deleted: ${issue.title}")
    }
}
