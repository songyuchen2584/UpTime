package com.example.healthconnect20


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val state by vm.state.collectAsState()

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = PermissionController.createRequestPermissionResultContract()
                ) { grantedPermissions: Set<String> ->
                    if (
                        grantedPermissions.contains(
                            HealthPermission.getReadPermission(StepsRecord::class)
                        )
                    ) {
                        // Permission granted
                    } else {
                        // Permission denied
                    }
                }

                // Your Compose UI
                Button(onClick = {
                    permissionLauncher.launch(
                        setOf(HealthPermission.getReadPermission(StepsRecord::class))
                    )
                }) {
                    Text("Request Steps Permission")
                }

                Surface(Modifier.fillMaxSize()) {
                    HealthConnectScreen(
                        state = state,
                        onRequestPermissions = {
                            permissionLauncher.launch(vm.permissionsToRequest)
                        },
                        onRefreshData = { vm.refreshData() },
                        onInstall = { startActivity(vm.installIntent()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthConnectScreen(
    state: UiState,
    onRequestPermissions: () -> Unit,
    onRefreshData: () -> Unit,
    onInstall: () -> Unit
) {
    val hasAllPerms = remember(state.granted) {
        // We requested two permissions; treat “granted contains both” as ready
        state.granted.any { it.contains("READ_STEPS") } && state.granted.any { it.contains("READ_EXERCISE") }
    }

    val formatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    Column(Modifier.padding(16.dp)) {
        Text("Health Connect Demo", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        when (state.sdkStatus) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Text("Health Connect is unavailable on this device.")
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                Text("Health Connect needs to be installed/updated.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = onInstall) { Text("Install / Update Health Connect") }
            }
            HealthConnectClient.SDK_AVAILABLE -> {
                Text("Health Connect is available.")
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRequestPermissions) { Text("Request permissions") }
                    OutlinedButton(
                        onClick = onRefreshData,
                        enabled = hasAllPerms && !state.loading
                    ) { Text("Read data") }
                }

                Spacer(Modifier.height(12.dp))

                Text("Granted permissions: ${state.granted.size}")
                Spacer(Modifier.height(8.dp))

                if (state.steps24h != null) {
                    Text("Steps (last 24h): ${state.steps24h}")
                    Spacer(Modifier.height(8.dp))
                }

                Text("Exercise sessions (last 7 days):")
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.sessions) { s ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                val start = s.start.atZone(ZoneId.systemDefault()).toLocalDateTime()
                                val end = s.end.atZone(ZoneId.systemDefault()).toLocalDateTime()
                                Text("Type: ${s.title}")
                                Text("Start: ${start.format(formatter)}")
                                Text("End:   ${end.format(formatter)}")
                            }
                        }
                    }
                }
            }
            else -> {
                Text("SDK status: ${state.sdkStatus}")
            }
        }

        if (state.loading) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
        }
    }
}
