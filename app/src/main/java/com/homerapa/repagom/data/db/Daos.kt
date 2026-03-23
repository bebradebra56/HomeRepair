package com.homerapa.repagom.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProject(id: Long): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: ProjectEntity): Long

    @Update
    suspend fun update(project: ProjectEntity)

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("SELECT COUNT(*) FROM projects")
    fun getProjectCount(): Flow<Int>
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE projectId = :projectId ORDER BY name ASC")
    fun getRoomsForProject(projectId: Long): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun getRoom(id: Long): Flow<RoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: RoomEntity): Long

    @Update
    suspend fun update(room: RoomEntity)

    @Delete
    suspend fun delete(room: RoomEntity)

    @Query("SELECT COUNT(*) FROM rooms WHERE projectId = :projectId")
    fun getRoomCount(projectId: Long): Flow<Int>
}

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getPhotosForProject(projectId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE roomId = :roomId ORDER BY createdAt DESC")
    fun getPhotosForRoom(roomId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhoto(id: Long): PhotoEntity?

    @Query("SELECT * FROM photos ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentPhotos(limit: Int = 10): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PhotoEntity): Long

    @Update
    suspend fun update(photo: PhotoEntity)

    @Delete
    suspend fun delete(photo: PhotoEntity)

    @Query("SELECT COUNT(*) FROM photos")
    fun getTotalPhotoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE projectId = :projectId")
    fun getPhotoCountForProject(projectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE roomId = :roomId")
    fun getPhotoCountForRoom(roomId: Long): Flow<Int>
}

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getIssuesForProject(projectId: Long): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE roomId = :roomId ORDER BY createdAt DESC")
    fun getIssuesForRoom(roomId: Long): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues ORDER BY createdAt DESC")
    fun getAllIssues(): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE id = :id")
    fun getIssue(id: Long): Flow<IssueEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(issue: IssueEntity): Long

    @Update
    suspend fun update(issue: IssueEntity)

    @Delete
    suspend fun delete(issue: IssueEntity)

    @Query("SELECT COUNT(*) FROM issues WHERE status NOT IN ('Fixed', 'Closed')")
    fun getOpenIssueCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM issues WHERE projectId = :projectId AND status NOT IN ('Fixed', 'Closed')")
    fun getOpenIssueCountForProject(projectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM issues WHERE roomId = :roomId AND status NOT IN ('Fixed', 'Closed')")
    fun getOpenIssueCountForRoom(roomId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM issues WHERE status IN ('Fixed', 'Closed')")
    fun getFixedIssueCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM issues")
    fun getTotalIssueCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM issues WHERE projectId = :projectId")
    fun getTotalIssueCountForProject(projectId: Long): Flow<Int>

    @Query("""
        SELECT * FROM issues WHERE projectId = :projectId AND status NOT IN ('Fixed', 'Closed')
        ORDER BY CASE priority WHEN 'Critical' THEN 1 WHEN 'High' THEN 2 WHEN 'Medium' THEN 3 ELSE 4 END
    """)
    fun getOpenIssuesForProject(projectId: Long): Flow<List<IssueEntity>>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getTasksForProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE roomId = :roomId ORDER BY deadline ASC")
    fun getTasksForRoom(roomId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTask(id: Long): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'In Progress'")
    fun getInProgressTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE projectId = :projectId AND status = 'In Progress'")
    fun getInProgressTaskCountForProject(projectId: Long): Flow<Int>
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activity_logs WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getActivitiesForProject(projectId: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentActivities(limit: Int = 20): Flow<List<ActivityEntity>>

    @Insert
    suspend fun insert(activity: ActivityEntity)

    @Query("DELETE FROM activity_logs WHERE id NOT IN (SELECT id FROM activity_logs ORDER BY timestamp DESC LIMIT 100)")
    suspend fun pruneOld()
}

@Dao
interface BeforeAfterDao {
    @Query("SELECT * FROM before_after WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getBeforeAfterForProject(projectId: Long): Flow<List<BeforeAfterEntity>>

    @Query("SELECT * FROM before_after WHERE roomId = :roomId ORDER BY createdAt DESC")
    fun getBeforeAfterForRoom(roomId: Long): Flow<List<BeforeAfterEntity>>

    @Query("SELECT * FROM before_after ORDER BY createdAt DESC")
    fun getAllBeforeAfter(): Flow<List<BeforeAfterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pair: BeforeAfterEntity): Long

    @Delete
    suspend fun delete(pair: BeforeAfterEntity)

    @Query("SELECT COUNT(*) FROM before_after WHERE projectId = :projectId")
    fun getCountForProject(projectId: Long): Flow<Int>
}
