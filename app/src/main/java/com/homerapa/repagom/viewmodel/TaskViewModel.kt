package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.db.TaskEntity
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val selectedTask: TaskEntity? = null,
    val isLoading: Boolean = true
)

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTasks().collect { tasks ->
                _uiState.update { it.copy(tasks = tasks, isLoading = false) }
            }
        }
    }

    fun loadTasksForProject(projectId: Long) = viewModelScope.launch {
        repository.getTasksForProject(projectId).collect { tasks ->
            _uiState.update { it.copy(tasks = tasks, isLoading = false) }
        }
    }

    fun loadTask(id: Long) = viewModelScope.launch {
        repository.getTask(id).collect { task ->
            _uiState.update { it.copy(selectedTask = task) }
        }
    }

    fun createTask(
        projectId: Long,
        roomId: Long?,
        issueId: Long?,
        title: String,
        description: String,
        deadline: Long?,
        priority: String,
        assignedTo: String,
        costEstimate: Double,
        notes: String
    ) = viewModelScope.launch {
        val task = TaskEntity(
            projectId = projectId,
            roomId = roomId,
            issueId = issueId,
            title = title,
            description = description,
            deadline = deadline,
            priority = priority,
            assignedTo = assignedTo,
            costEstimate = costEstimate,
            notes = notes
        )
        repository.insertTask(task)
        repository.logActivity(projectId, "Task Created", "Created task: $title")
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: String) = viewModelScope.launch {
        repository.updateTask(task.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        repository.logActivity(task.projectId, "Task Updated", "${task.title} → $newStatus")
    }

    fun updateTask(task: TaskEntity) = viewModelScope.launch {
        repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch {
        repository.deleteTask(task)
        repository.logActivity(task.projectId, "Task Deleted", "Deleted: ${task.title}")
    }
}
