package com.example.uptime.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserProfileOverlay(
    profile: FriendProfile?,
    isFriend: Boolean,
    onAddFriend: () -> Unit,
    onRemoveFriend: () -> Unit,
    onVisitRoom: () -> Unit,
    onDismiss: () -> Unit,
    isAnon: Boolean
) {
    AnimatedVisibility(
        visible = profile != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            profile?.let {
                UserProfileCard(
                    profile = it,
                    isFriend = isFriend,
                    onAddFriend = onAddFriend,
                    onRemoveFriend = onRemoveFriend,
                    onVisitRoom = onVisitRoom,
                    onDismiss = onDismiss,
                    isAnon = isAnon,
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}