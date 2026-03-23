package com.homerapa.repagom

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.homerapa.repagom.ui.navigation.AppNavGraph
import com.homerapa.repagom.ui.navigation.Screen
import com.homerapa.repagom.ui.navigation.bottomNavRoutes
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.AppViewModel

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, Icons.Default.Home, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.Projects, Icons.Default.FolderOpen, Icons.Default.Folder, "Projects"),
    BottomNavItem(Screen.Issues, Icons.Default.ReportProblem, Icons.Default.Warning, "Issues"),
    BottomNavItem(Screen.Reports, Icons.Default.BarChart, Icons.Default.InsertChart, "Reports"),
    BottomNavItem(Screen.Profile, Icons.Default.Person, Icons.Default.AccountCircle, "Profile")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val prefs by appViewModel.userPreferences.collectAsStateWithLifecycle()

            val darkTheme = when (prefs.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            HomeRepairTheme(darkTheme = darkTheme) {
                val context = LocalContext.current
                LaunchedEffect(darkTheme) {
                    (context as? Activity)?.window?.let { window ->
                        WindowCompat.getInsetsController(window, window.decorView)
                            .isAppearanceLightStatusBars = !darkTheme
                    }
                }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute in bottomNavRoutes

                val startDestination = Screen.Splash.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            AppBottomNavigationBar(
                                currentRoute = currentRoute,
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    AppNavGraph(
                        navController = navController,
                        appViewModel = appViewModel,
                        startDestination = startDestination,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        shadowElevation = 16.dp,
        color = NavyBlue
    ) {
        NavigationBar(
            containerColor = NavyBlue,
            contentColor = Color.White,
            modifier = Modifier
                .navigationBarsPadding()
                .height(68.dp)
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.screen.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onNavigate(item.screen) },
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ConstructionYellow)
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        item.selectedIcon,
                                        contentDescription = item.label,
                                        tint = NavyBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = Color.White.copy(alpha = 0.55f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    },
                    label = {
                        Text(
                            item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) ConstructionYellow else Color.White.copy(alpha = 0.55f)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
