package com.homerapa.repagom.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.homerapa.repagom.viewmodel.AppViewModel
import com.homerapa.repagom.ui.screens.splash.SplashScreen
import com.homerapa.repagom.ui.screens.onboarding.OnboardingScreen
import com.homerapa.repagom.ui.screens.dashboard.DashboardScreen
import com.homerapa.repagom.ui.screens.projects.ProjectsScreen
import com.homerapa.repagom.ui.screens.projects.CreateProjectScreen
import com.homerapa.repagom.ui.screens.projects.ProjectDetailScreen
import com.homerapa.repagom.ui.screens.rooms.CreateRoomScreen
import com.homerapa.repagom.ui.screens.rooms.RoomDetailScreen
import com.homerapa.repagom.ui.screens.photos.AddPhotoScreen
import com.homerapa.repagom.ui.screens.photos.PhotoViewerScreen
import com.homerapa.repagom.ui.screens.issues.IssuesScreen
import com.homerapa.repagom.ui.screens.issues.CreateIssueScreen
import com.homerapa.repagom.ui.screens.issues.IssueDetailScreen
import com.homerapa.repagom.ui.screens.tasks.TasksScreen
import com.homerapa.repagom.ui.screens.tasks.CreateTaskScreen
import com.homerapa.repagom.ui.screens.beforeafter.BeforeAfterScreen
import com.homerapa.repagom.ui.screens.reports.ReportsScreen
import com.homerapa.repagom.ui.screens.profile.ProfileScreen
import com.homerapa.repagom.ui.screens.settings.SettingsScreen
import com.homerapa.repagom.ui.screens.activity.ActivityHistoryScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appViewModel: AppViewModel,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = { isOnboardingDone ->
                    val dest = if (isOnboardingDone) Screen.Dashboard.route else Screen.Onboarding.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    appViewModel.setOnboardingComplete()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                appViewModel = appViewModel,
                onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
                onNavigateToProject = { projectId -> navController.navigate(Screen.ProjectDetail.createRoute(projectId)) },
                onNavigateToIssues = { navController.navigate(Screen.Issues.route) },
                onNavigateToAddPhoto = { projectId -> navController.navigate(Screen.AddPhoto.createRoute(projectId)) },
                onNavigateToCreateProject = { navController.navigate(Screen.CreateProject.route) },
                onNavigateToActivityHistory = { navController.navigate(Screen.ActivityHistory.route) }
            )
        }

        composable(Screen.Projects.route) {
            ProjectsScreen(
                onNavigateToProject = { projectId -> navController.navigate(Screen.ProjectDetail.createRoute(projectId)) },
                onNavigateToCreateProject = { navController.navigate(Screen.CreateProject.route) }
            )
        }

        composable(Screen.CreateProject.route) {
            CreateProjectScreen(
                onBack = { navController.popBackStack() },
                onProjectCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditProject.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
            CreateProjectScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() },
                onProjectCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ProjectDetail.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
            ProjectDetailScreen(
                projectId = projectId,
                appViewModel = appViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToRoom = { roomId -> navController.navigate(Screen.RoomDetail.createRoute(roomId)) },
                onNavigateToCreateRoom = { navController.navigate(Screen.CreateRoom.createRoute(projectId)) },
                onNavigateToAddPhoto = { navController.navigate(Screen.AddPhoto.createRoute(projectId)) },
                onNavigateToIssues = { navController.navigate(Screen.Issues.route) },
                onNavigateToTasks = { navController.navigate(Screen.Tasks.createRoute(projectId)) },
                onNavigateToBeforeAfter = { navController.navigate(Screen.BeforeAfter.createRoute(projectId)) },
                onNavigateToEdit = { navController.navigate(Screen.EditProject.createRoute(projectId)) }
            )
        }

        composable(
            route = Screen.CreateRoom.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
            CreateRoomScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() },
                onRoomCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RoomDetail.route,
            arguments = listOf(navArgument("roomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: return@composable
            RoomDetailScreen(
                roomId = roomId,
                onBack = { navController.popBackStack() },
                onNavigateToAddPhoto = { projectId -> navController.navigate(Screen.AddPhoto.createRoute(projectId, roomId)) },
                onNavigateToPhoto = { photoId, projectId -> navController.navigate(Screen.PhotoViewer.createRoute(photoId, projectId)) },
                onNavigateToCreateIssue = { projectId -> navController.navigate(Screen.CreateIssue.createRoute(projectId, roomId)) },
                onNavigateToIssue = { issueId -> navController.navigate(Screen.IssueDetail.createRoute(issueId)) },
                onNavigateToCreateTask = { projectId -> navController.navigate(Screen.CreateTask.createRoute(projectId, roomId)) }
            )
        }

        composable(
            route = Screen.AddPhoto.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType },
                navArgument("roomId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            AddPhotoScreen(
                projectId = projectId,
                roomId = if (roomId == 0L) null else roomId,
                onBack = { navController.popBackStack() },
                onPhotoSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PhotoViewer.route,
            arguments = listOf(
                navArgument("photoId") { type = NavType.LongType },
                navArgument("projectId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getLong("photoId") ?: return@composable
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
            PhotoViewerScreen(
                photoId = photoId,
                projectId = projectId,
                onBack = { navController.popBackStack() },
                onNavigateToCreateIssue = { pId, phId ->
                    navController.navigate(Screen.CreateIssue.createRoute(pId, 0L, phId))
                }
            )
        }

        composable(Screen.Issues.route) {
            IssuesScreen(
                onNavigateToIssue = { issueId -> navController.navigate(Screen.IssueDetail.createRoute(issueId)) },
                onNavigateToCreateIssue = { navController.navigate(Screen.CreateIssue.createRoute(0L)) }
            )
        }

        composable(
            route = Screen.CreateIssue.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("roomId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("photoId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            val photoId = backStackEntry.arguments?.getLong("photoId") ?: 0L
            CreateIssueScreen(
                projectId = if (projectId == 0L) null else projectId,
                roomId = if (roomId == 0L) null else roomId,
                photoId = if (photoId == 0L) null else photoId,
                onBack = { navController.popBackStack() },
                onIssueCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.IssueDetail.route,
            arguments = listOf(navArgument("issueId") { type = NavType.LongType })
        ) { backStackEntry ->
            val issueId = backStackEntry.arguments?.getLong("issueId") ?: return@composable
            IssueDetailScreen(
                issueId = issueId,
                onBack = { navController.popBackStack() },
                onNavigateToPhoto = { photoId, projectId ->
                    navController.navigate(Screen.PhotoViewer.createRoute(photoId, projectId))
                },
                onNavigateToCreateTask = { projectId, iId ->
                    navController.navigate(Screen.CreateTask.createRoute(projectId, 0L, iId))
                }
            )
        }

        composable(
            route = Screen.Tasks.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
            TasksScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() },
                onNavigateToCreateTask = { navController.navigate(Screen.CreateTask.createRoute(projectId)) }
            )
        }

        composable(
            route = Screen.CreateTask.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("roomId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("issueId") { type = NavType.LongType; defaultValue = 0L }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
            val issueId = backStackEntry.arguments?.getLong("issueId") ?: 0L
            CreateTaskScreen(
                projectId = if (projectId == 0L) null else projectId,
                roomId = if (roomId == 0L) null else roomId,
                issueId = if (issueId == 0L) null else issueId,
                onBack = { navController.popBackStack() },
                onTaskCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BeforeAfter.route,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: return@composable
            BeforeAfterScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateToActivityHistory = { navController.navigate(Screen.ActivityHistory.route) }
            )
        }

        composable(Screen.ActivityHistory.route) {
            ActivityHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                appViewModel = appViewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToActivityHistory = { navController.navigate(Screen.ActivityHistory.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
