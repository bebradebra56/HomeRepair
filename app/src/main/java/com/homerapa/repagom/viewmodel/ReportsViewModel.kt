package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.db.IssueEntity
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReportsUiState(
    val totalPhotos: Int = 0,
    val totalIssues: Int = 0,
    val openIssues: Int = 0,
    val fixedIssues: Int = 0,
    val inProgressTasks: Int = 0,
    val allIssues: List<IssueEntity> = emptyList(),
    val isLoading: Boolean = true
)

class ReportsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getTotalPhotoCount(),
                repository.getTotalIssueCount(),
                repository.getOpenIssueCount(),
                repository.getFixedIssueCount(),
                repository.getInProgressTaskCount()
            ) { photos, total, open, fixed, tasks ->
                ReportsUiState(
                    totalPhotos = photos,
                    totalIssues = total,
                    openIssues = open,
                    fixedIssues = fixed,
                    inProgressTasks = tasks,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }

        viewModelScope.launch {
            repository.getAllIssues().collect { issues ->
                _uiState.update { it.copy(allIssues = issues) }
            }
        }
    }
}
