package com.example.uptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.example.uptime.room.RoomScreen
import com.example.uptime.room.RoomViewModel
import com.example.uptime.screentime.ScreenTimeRoute
import com.example.uptime.ui.theme.UpTimeTheme
import com.example.uptime.walking.WalkingRoute
import kotlinx.coroutines.launch
import kotlin.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val roomViewModel: RoomViewModel by viewModels()
        val dashboardViewModel: DashboardViewModel by viewModels()

        setContent {
            UpTimeTheme {
                AppScaffold(
                    roomViewModel = roomViewModel,
                    dashboardViewModel = dashboardViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppScaffold(roomViewModel: RoomViewModel, dashboardViewModel: DashboardViewModel) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isLandscape = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    val backStack = rememberNavBackStack(NavDestination.Dashboard)
    val currentDestination = backStack.last()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        roomViewModel.newlyUnlocked.collect { achievements ->
            achievements.forEach { achievement ->
                scope.launch {
                    snackbarHostState.showSnackbar("Unlocked: ${achievement.title}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopBar(onSettingsClick = {
                if (currentDestination != NavDestination.Settings) {
                    backStack.add(NavDestination.Settings)
                }
            })
        },
        bottomBar = {
            if (!isLandscape) {
                NavigationBar {
                    NavDestination.all.forEach { dest ->
                        NavigationBarItem(
                            selected = currentDestination == dest,
                            onClick  = {
                                if (currentDestination != dest) { backStack.add(dest) }
                            },
                            icon  = { Icon(painterResource(dest.icon), contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLandscape) {
                NavigationRail {
                    NavDestination.all.forEach { dest ->
                        NavigationRailItem(
                            selected = currentDestination == dest,
                            onClick  = {
                                if (currentDestination != dest) { backStack.add(dest) }
                            },
                            icon  = { Icon(painterResource(dest.icon), contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }

            BackHandler(enabled = backStack.size > 1) {
                backStack.removeLastOrNull()
            }

            NavDisplay(
                backStack = backStack,
                modifier = Modifier.weight(1f),
                onBack = { backStack.removeLastOrNull() },
                transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                entryProvider = entryProvider {
                    entry<NavDestination> { destination ->
                        when (destination) {
                            NavDestination.Dashboard -> DashboardScreen(
                                onNavigateToStreak = { backStack.add(NavDestination.Streak) },
                                onNavigateToWalkingProgress = { backStack.add(NavDestination.Walking) },
                                onNavigateToScreenTime = { backStack.add(NavDestination.ScreenTime) },
                                viewModel = dashboardViewModel
                            )
                            NavDestination.Streak -> StreakScreen()
                            NavDestination.Room -> RoomScreen(viewModel = roomViewModel)
                            NavDestination.Walking -> WalkingRoute()
                            NavDestination.ScreenTime -> ScreenTimeRoute(
                                updateScreenTime = dashboardViewModel::updateScreenTime
                            )
                            NavDestination.Settings -> SettingsScreen()
                        }
                    }
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "UpTime",
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(painterResource(R.drawable.settings_24px), contentDescription = "Options")
            }
        }
    )
}

@Preview
@Composable
fun NavPreview() {
//    val roomViewModel: RoomViewModel by viewModels()
//    val dashboardViewModel: DashboardViewModel by viewModels()
//
//    UpTimeTheme {
//        AppScaffold(roomViewModel, dashboardViewModel)
//    }
}