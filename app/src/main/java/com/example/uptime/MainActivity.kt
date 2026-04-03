package com.example.uptime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass
import com.example.uptime.ui.theme.UpTimeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpTimeTheme {
                AppScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppScaffold() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isLandscape = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    var selectedDestination by remember { mutableStateOf<NavDestination>(NavDestination.Dashboard) }

    Scaffold(
        topBar = {
            TopBar(onSettingsClick = { selectedDestination = NavDestination.Settings })
        },
        bottomBar = {
            if (!isLandscape) {
                NavigationBar {
                    NavDestination.all.forEach { dest ->
                        NavigationBarItem(
                            selected = selectedDestination == dest,
                            onClick = { selectedDestination = dest },
                            icon = { Icon(painterResource(id = dest.icon), contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLandscape) {
                NavigationRail {
                    NavDestination.all.forEach { dest ->
                        NavigationRailItem(
                            selected = selectedDestination == dest,
                            onClick = { selectedDestination = dest },
                            icon = { Icon(painterResource(id = dest.icon), contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                CurrentScreen(selectedDestination, onNavigate = { selectedDestination = it })
            }
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

@Composable
fun CurrentScreen(
    destination: NavDestination, onNavigate: (NavDestination) -> Unit) {
    when (destination) {
        NavDestination.Dashboard -> DashboardScreen(
            onNavigateToStreak = { onNavigate(NavDestination.Streak) }
        )
        NavDestination.Streak    -> StreakScreen()
        NavDestination.Room      -> RoomScreen()
        NavDestination.Settings  -> SettingsScreen()
    }
}

@Preview
@Composable
fun NavPreview() {
    UpTimeTheme {
        AppScaffold()
    }
}