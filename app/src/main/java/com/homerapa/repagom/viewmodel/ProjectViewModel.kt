package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.db.ProjectEntity
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProjectUiState(
    val projects: List<ProjectEntity> = emptyList(),
    val selectedProject: ProjectEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProjectViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository

    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllProjects().collect { projects ->
                _uiState.update { it.copy(projects = projects, isLoading = false) }
            }
        }
    }

    fun loadProject(id: Long) = viewModelScope.launch {
        repository.getProject(id).collect { project ->
            _uiState.update { it.copy(selectedProject = project) }
        }
    }

    fun createProject(
        name: String,
        propertyType: String,
        address: String,
        startDate: Long,
        notes: String,
        coverPhotoUri: String?
    ) = viewModelScope.launch {
        val project = ProjectEntity(
            name = name,
            propertyType = propertyType,
            address = address,
            startDate = startDate,
            notes = notes,
            coverPhotoUri = coverPhotoUri
        )
        repository.insertProject(project)
        repository.logActivity(action = "Project Created", description = "Created project: $name")
    }

    fun updateProject(project: ProjectEntity) = viewModelScope.launch {
        repository.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
        repository.logActivity(project.id, "Project Updated", "Updated: ${project.name}")
    }

    fun deleteProject(project: ProjectEntity) = viewModelScope.launch {
        repository.deleteProject(project)
        repository.logActivity(action = "Project Deleted", description = "Deleted: ${project.name}")
    }
}
