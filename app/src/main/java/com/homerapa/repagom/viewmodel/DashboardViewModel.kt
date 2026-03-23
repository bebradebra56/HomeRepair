package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.db.IssueEntity
import com.homerapa.repagom.data.db.PhotoEntity
import com.homerapa.repagom.data.db.ProjectEntity
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val activeProject: ProjectEntity? = null,
    val openIssuesCount: Int = 0,
    val photosCount: Int = 0,
    val inProgressTasksCount: Int = 0,
    val beforeAfterCount: Int = 0,
    val recentPhotos: List<PhotoEntity> = emptyList(),
    val recentIssues: List<IssueEntity> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository
    private val prefsRepo = (app as HomeRepairApplication).preferencesRepository

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.preferences.collectLatest { prefs ->
                var projectId = prefs.activeProjectId
                if (projectId == -1L) {
                    val allProjects = repository.getAllProjects().first()
                    if (allProjects.isNotEmpty()) {
                        projectId = allProjects.first().id
                        prefsRepo.setActiveProject(projectId)
                        return@collectLatest
                    }
                    _uiState.update { it.copy(isLoading = false, activeProject = null) }
                    return@collectLatest
                }
                combine(
                    repository.getProject(projectId).filterNotNull(),
                    repository.getOpenIssueCountForProject(projectId),
                    repository.getPhotoCountForProject(projectId),
                    repository.getInProgressTaskCountForProject(projectId),
                    repository.getBeforeAfterCountForProject(projectId)
                ) { project, openIssues, photos, tasks, beforeAfter ->
                    _uiState.value.copy(
                        activeProject = project,
                        openIssuesCount = openIssues,
                        photosCount = photos,
                        inProgressTasksCount = tasks,
                        beforeAfterCount = beforeAfter,
                        isLoading = false
                    )
                }.collect { state -> _uiState.value = state }
            }
        }

        viewModelScope.launch {
            repository.getRecentPhotos(6).collect { photos ->
                _uiState.update { it.copy(recentPhotos = photos) }
            }
        }

        viewModelScope.launch {
            repository.getAllIssues().map { it.take(5) }.collect { issues ->
                _uiState.update { it.copy(recentIssues = issues) }
            }
        }
    }
}
