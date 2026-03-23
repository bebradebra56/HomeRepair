package com.homerapa.repagom.data.repository

import com.homerapa.repagom.data.db.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    // --- Projects ---
    fun getAllProjects(): Flow<List<ProjectEntity>> = db.projectDao().getAllProjects()
    fun getProject(id: Long): Flow<ProjectEntity?> = db.projectDao().getProject(id)
    fun getProjectCount(): Flow<Int> = db.projectDao().getProjectCount()
    suspend fun insertProject(project: ProjectEntity): Long = db.projectDao().insert(project)
    suspend fun updateProject(project: ProjectEntity) = db.projectDao().update(project)
    suspend fun deleteProject(project: ProjectEntity) = db.projectDao().delete(project)

    // --- Rooms ---
    fun getRoomsForProject(projectId: Long): Flow<List<RoomEntity>> = db.roomDao().getRoomsForProject(projectId)
    fun getRoom(id: Long): Flow<RoomEntity?> = db.roomDao().getRoom(id)
    fun getRoomCount(projectId: Long): Flow<Int> = db.roomDao().getRoomCount(projectId)
    suspend fun insertRoom(room: RoomEntity): Long = db.roomDao().insert(room)
    suspend fun updateRoom(room: RoomEntity) = db.roomDao().update(room)
    suspend fun deleteRoom(room: RoomEntity) = db.roomDao().delete(room)

    // --- Photos ---
    fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>> = db.photoDao().getPhotosForProject(projectId)
    fun getPhotosForRoom(roomId: Long): Flow<List<PhotoEntity>> = db.photoDao().getPhotosForRoom(roomId)
    fun getRecentPhotos(limit: Int = 10): Flow<List<PhotoEntity>> = db.photoDao().getRecentPhotos(limit)
    fun getTotalPhotoCount(): Flow<Int> = db.photoDao().getTotalPhotoCount()
    fun getPhotoCountForProject(projectId: Long): Flow<Int> = db.photoDao().getPhotoCountForProject(projectId)
    fun getPhotoCountForRoom(roomId: Long): Flow<Int> = db.photoDao().getPhotoCountForRoom(roomId)
    suspend fun getPhoto(id: Long): PhotoEntity? = db.photoDao().getPhoto(id)
    suspend fun insertPhoto(photo: PhotoEntity): Long = db.photoDao().insert(photo)
    suspend fun updatePhoto(photo: PhotoEntity) = db.photoDao().update(photo)
    suspend fun deletePhoto(photo: PhotoEntity) = db.photoDao().delete(photo)

    // --- Issues ---
    fun getAllIssues(): Flow<List<IssueEntity>> = db.issueDao().getAllIssues()
    fun getIssuesForProject(projectId: Long): Flow<List<IssueEntity>> = db.issueDao().getIssuesForProject(projectId)
    fun getIssuesForRoom(roomId: Long): Flow<List<IssueEntity>> = db.issueDao().getIssuesForRoom(roomId)
    fun getIssue(id: Long): Flow<IssueEntity?> = db.issueDao().getIssue(id)
    fun getOpenIssueCount(): Flow<Int> = db.issueDao().getOpenIssueCount()
    fun getOpenIssueCountForProject(projectId: Long): Flow<Int> = db.issueDao().getOpenIssueCountForProject(projectId)
    fun getOpenIssueCountForRoom(roomId: Long): Flow<Int> = db.issueDao().getOpenIssueCountForRoom(roomId)
    fun getFixedIssueCount(): Flow<Int> = db.issueDao().getFixedIssueCount()
    fun getTotalIssueCount(): Flow<Int> = db.issueDao().getTotalIssueCount()
    fun getTotalIssueCountForProject(projectId: Long): Flow<Int> = db.issueDao().getTotalIssueCountForProject(projectId)
    fun getOpenIssuesForProject(projectId: Long): Flow<List<IssueEntity>> = db.issueDao().getOpenIssuesForProject(projectId)
    suspend fun insertIssue(issue: IssueEntity): Long = db.issueDao().insert(issue)
    suspend fun updateIssue(issue: IssueEntity) = db.issueDao().update(issue)
    suspend fun deleteIssue(issue: IssueEntity) = db.issueDao().delete(issue)

    // --- Tasks ---
    fun getAllTasks(): Flow<List<TaskEntity>> = db.taskDao().getAllTasks()
    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>> = db.taskDao().getTasksForProject(projectId)
    fun getTasksForRoom(roomId: Long): Flow<List<TaskEntity>> = db.taskDao().getTasksForRoom(roomId)
    fun getTask(id: Long): Flow<TaskEntity?> = db.taskDao().getTask(id)
    fun getInProgressTaskCount(): Flow<Int> = db.taskDao().getInProgressTaskCount()
    fun getInProgressTaskCountForProject(projectId: Long): Flow<Int> = db.taskDao().getInProgressTaskCountForProject(projectId)
    suspend fun insertTask(task: TaskEntity): Long = db.taskDao().insert(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().update(task)
    suspend fun deleteTask(task: TaskEntity) = db.taskDao().delete(task)

    // --- Activities ---
    fun getAllActivities(): Flow<List<ActivityEntity>> = db.activityDao().getAllActivities()
    fun getRecentActivities(limit: Int = 20): Flow<List<ActivityEntity>> = db.activityDao().getRecentActivities(limit)
    fun getActivitiesForProject(projectId: Long): Flow<List<ActivityEntity>> = db.activityDao().getActivitiesForProject(projectId)
    suspend fun logActivity(projectId: Long? = null, action: String, description: String) {
        db.activityDao().insert(ActivityEntity(projectId = projectId, action = action, description = description))
        db.activityDao().pruneOld()
    }

    // --- Before/After ---
    fun getAllBeforeAfter(): Flow<List<BeforeAfterEntity>> = db.beforeAfterDao().getAllBeforeAfter()
    fun getBeforeAfterForProject(projectId: Long): Flow<List<BeforeAfterEntity>> = db.beforeAfterDao().getBeforeAfterForProject(projectId)
    fun getBeforeAfterForRoom(roomId: Long): Flow<List<BeforeAfterEntity>> = db.beforeAfterDao().getBeforeAfterForRoom(roomId)
    fun getBeforeAfterCountForProject(projectId: Long): Flow<Int> = db.beforeAfterDao().getCountForProject(projectId)
    suspend fun insertBeforeAfter(pair: BeforeAfterEntity): Long = db.beforeAfterDao().insert(pair)
    suspend fun deleteBeforeAfter(pair: BeforeAfterEntity) = db.beforeAfterDao().delete(pair)
}
