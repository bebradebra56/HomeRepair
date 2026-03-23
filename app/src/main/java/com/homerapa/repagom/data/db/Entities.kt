package com.homerapa.repagom.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val propertyType: String = "Apartment",
    val address: String = "",
    val startDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val coverPhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId")]
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val category: String = "Other",
    val area: Float = 0f,
    val floor: Int = 1,
    val notes: String = "",
    val coverPhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("roomId")]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long? = null,
    val uri: String,
    val label: String = "Before",
    val note: String = "",
    val takenAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "issues",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("roomId"), Index("photoId")]
)
data class IssueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long? = null,
    val photoId: Long? = null,
    val title: String,
    val description: String = "",
    val category: String = "Other",
    val priority: String = "Medium",
    val status: String = "New",
    val assignedTo: String = "",
    val deadline: Long? = null,
    val markerX: Float? = null,
    val markerY: Float? = null,
    val notes: String = "",
    val afterPhotoUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("projectId"), Index("roomId"), Index("issueId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long? = null,
    val issueId: Long? = null,
    val title: String,
    val description: String = "",
    val deadline: Long? = null,
    val priority: String = "Medium",
    val assignedTo: String = "",
    val costEstimate: Double = 0.0,
    val status: String = "Todo",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val action: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "before_after",
    indices = [Index("projectId"), Index("beforePhotoId"), Index("afterPhotoId")]
)
data class BeforeAfterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val roomId: Long? = null,
    val issueId: Long? = null,
    val beforePhotoId: Long,
    val afterPhotoId: Long,
    val resultNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
