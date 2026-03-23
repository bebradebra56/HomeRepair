package com.homerapa.repagom.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Projects : Screen("projects")
    object CreateProject : Screen("create_project")
    object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: Long) = "project_detail/$projectId"
    }
    object EditProject : Screen("edit_project/{projectId}") {
        fun createRoute(projectId: Long) = "edit_project/$projectId"
    }
    object CreateRoom : Screen("create_room/{projectId}") {
        fun createRoute(projectId: Long) = "create_room/$projectId"
    }
    object RoomDetail : Screen("room_detail/{roomId}") {
        fun createRoute(roomId: Long) = "room_detail/$roomId"
    }
    object AddPhoto : Screen("add_photo/{projectId}/{roomId}") {
        fun createRoute(projectId: Long, roomId: Long = 0L) = "add_photo/$projectId/$roomId"
    }
    object PhotoViewer : Screen("photo_viewer/{photoId}/{projectId}") {
        fun createRoute(photoId: Long, projectId: Long) = "photo_viewer/$photoId/$projectId"
    }
    object Issues : Screen("issues")
    object CreateIssue : Screen("create_issue/{projectId}/{roomId}/{photoId}") {
        fun createRoute(projectId: Long, roomId: Long = 0L, photoId: Long = 0L) =
            "create_issue/$projectId/$roomId/$photoId"
    }
    object IssueDetail : Screen("issue_detail/{issueId}") {
        fun createRoute(issueId: Long) = "issue_detail/$issueId"
    }
    object Tasks : Screen("tasks/{projectId}") {
        fun createRoute(projectId: Long) = "tasks/$projectId"
    }
    object CreateTask : Screen("create_task/{projectId}/{roomId}/{issueId}") {
        fun createRoute(projectId: Long, roomId: Long = 0L, issueId: Long = 0L) =
            "create_task/$projectId/$roomId/$issueId"
    }
    object BeforeAfter : Screen("before_after/{projectId}") {
        fun createRoute(projectId: Long) = "before_after/$projectId"
    }
    object Reports : Screen("reports")
    object ActivityHistory : Screen("activity_history")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}

val bottomNavRoutes = setOf(
    Screen.Dashboard.route,
    Screen.Projects.route,
    Screen.Issues.route,
    Screen.Reports.route,
    Screen.Profile.route
)

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val iconRes: String
)
