package com.example.uptime.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.profile.FriendProfile
import com.example.uptime.R
import com.example.uptime.auth.AuthViewModel
import com.example.uptime.dashboard.DashboardViewModel
import com.example.uptime.room.RoomViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ProfileScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val stats by dashboardViewModel.userStats.collectAsState()
    val authState by authViewModel.state.collectAsState()
    val isSignedIn = !authState.isAnonymous
    var showEditName by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.Companion.height(24.dp))

        ProfileHeader(
            isAnonymous = authState.isAnonymous,
            displayName = authState.displayName,
            email = authState.user?.email
        )
        if (!authState.isAnonymous) {
            TextButton(onClick = {
                showEditName = true
                editNameText = authState.displayName ?: ""
            }) {
                Text("Edit Name")
            }
        }

        if (showEditName) {
            AlertDialog(
                onDismissRequest = { showEditName = false },
                title = { Text("Edit Name") },
                text = {
                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { editNameText = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.Companion.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authViewModel.updateName(editNameText)
                            showEditName = false
                        },
                        enabled = editNameText.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditName = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        Spacer(modifier = Modifier.Companion.height(20.dp))

        StatsOverviewCard(
            currentStreak = stats.currentStreak,
            trophiesUnlocked = roomViewModel.totalUnlockedAchievements.collectAsState().value
        )

        Spacer(modifier = Modifier.Companion.height(16.dp))

        if (authState.isAnonymous) {
            Card(
                modifier = Modifier.Companion.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.Companion.padding(20.dp),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Button(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.Companion.fillMaxWidth()
                    ) {
                        Text("Sign in to save your progress")
                    }
                }
            }

            Spacer(modifier = Modifier.Companion.height(16.dp))

            FriendsCardPlaceholder()
        } else {
            FriendsListCard(authViewModel = authViewModel)

            Spacer(modifier = Modifier.Companion.height(64.dp))

            OutlinedButton(
                onClick = { authViewModel.signOut() },
                modifier = Modifier.Companion.fillMaxWidth()
            ) {
                Text("Sign Out")
            }
        }

        Spacer(modifier = Modifier.Companion.height(24.dp))
    }
}

@Composable
fun ProfileHeader(isAnonymous: Boolean, displayName: String?, email: String?) {
    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        modifier = Modifier.Companion.fillMaxWidth()
    ) {
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ),
            modifier = Modifier.Companion.size(80.dp)
        ) {
            Column(
                modifier = Modifier.Companion.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                Icon(
                    painterResource(R.drawable.person_24px),
                    contentDescription = "Profile",
                    modifier = Modifier.Companion.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.Companion.height(12.dp))

        if (isAnonymous) {
            Text(
                text = "Guest",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Companion.Bold
            )
            Text(
                text = "Sign in to save your progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = if (!displayName.isNullOrBlank()) displayName else email ?: "User",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Companion.Bold
            )
            if (!email.isNullOrBlank()) {
                Spacer(modifier = Modifier.Companion.height(2.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatsOverviewCard(currentStreak: Int, trophiesUnlocked: Int) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                Icon(
                    painterResource(R.drawable.streak_24px),
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$currentStreak",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Companion.Bold
                )
                Text(
                    text = "Day streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                Icon(
                    painterResource(R.drawable.trophy_24px),
                    contentDescription = "Achievements",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "$trophiesUnlocked",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Companion.Bold
                )
                Text(
                    text = "Trophies",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FriendsCardPlaceholder() {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.Companion.padding(20.dp),
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            Text(
                text = "Friends",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Companion.SemiBold,
                modifier = Modifier.Companion.fillMaxWidth()
            )

            Spacer(modifier = Modifier.Companion.height(16.dp))

            Text(
                text = "Sign in to add friends and see their streaks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.fillMaxWidth()
            )
        }
    }
}

@Composable
fun FriendsListCard(authViewModel: AuthViewModel) {
    val friends by authViewModel.friendsRepository.observeFriends()
        .collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var addError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.Companion.padding(20.dp)) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Text(
                    text = "Friends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Companion.SemiBold
                )
                TextButton(onClick = { showAddDialog = true }) {
                    Text("+ Add")
                }
            }

            Spacer(modifier = Modifier.Companion.height(8.dp))

            if (friends.isEmpty()) {
                Text(
                    text = "No friends yet. Add someone by their email or visit their Room!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Companion.Center,
                    modifier = Modifier.Companion.fillMaxWidth()
                )
            } else {
                friends
                    .sortedWith(compareByDescending<FriendProfile> { it.trophies }
                        .thenByDescending { it.streak })
                    .forEach { friend ->
                        FriendRow(
                            friend = friend,
                            onRemove = {
                                scope.launch {
                                    authViewModel.friendsRepository.removeFriend(friend.uid)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.Companion.height(8.dp))
                    }
            }
        }
    }

    if (showAddDialog) {
        AddFriendDialog(
            error = addError,
            onDismiss = {
                showAddDialog = false
                addError = null
            },
            onAdd = { email ->
                scope.launch {
                    val result = authViewModel.friendsRepository.addFriendByEmail(email)
                    result.fold(
                        onSuccess = {
                            showAddDialog = false
                            addError = null
                        },
                        onFailure = {
                            addError = it.message
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun FriendRow(friend: FriendProfile, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // avatar
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                modifier = Modifier.Companion.size(36.dp)
            ) {
                Column(
                    modifier = Modifier.Companion.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Icon(
                        painterResource(R.drawable.person_24px),
                        contentDescription = null,
                        modifier = Modifier.Companion.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.Companion.width(12.dp))

            // info
            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    text = friend.name.ifBlank { friend.email },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Companion.Medium
                )
                Row {
                    Icon(
                        painterResource(R.drawable.streak_24px),
                        contentDescription = "Streak",
                        modifier = Modifier.Companion.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " ${friend.streak}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.Companion.width(12.dp))
                    Icon(
                        painterResource(R.drawable.trophy_24px),
                        contentDescription = "Achievements",
                        modifier = Modifier.Companion.size(18.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = " ${friend.trophies}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // remove button
            IconButton(
                onClick = onRemove,
                modifier = Modifier.Companion.size(32.dp)
            ) {
                Icon(
                    painterResource(R.drawable.close_24px),
                    contentDescription = "Remove friend",
                    modifier = Modifier.Companion.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    error: String?,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend") },
        text = {
            Column {
                Text(
                    text = "Enter your friend's email address",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.Companion.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.Companion.fillMaxWidth()
                )
                error?.let {
                    Spacer(modifier = Modifier.Companion.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(email) },
                enabled = email.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}