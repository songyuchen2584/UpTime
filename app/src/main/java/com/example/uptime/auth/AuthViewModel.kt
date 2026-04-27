package com.example.uptime.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptime.profile.FriendsRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val user: FirebaseUser? = null,
    val isAnonymous: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
    val displayName: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    val friendsRepository = FriendsRepository()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _state.value = AuthState(
                user = currentUser,
                isAnonymous = currentUser.isAnonymous,
                displayName = currentUser.displayName
            )
            // load name from Firestore if not in Firebase Auth
            if (!currentUser.isAnonymous && currentUser.displayName.isNullOrBlank()) {
                viewModelScope.launch {
                    val name = friendsRepository.getCurrentUserName()
                    if (name != null) {
                        _state.value = _state.value.copy(displayName = name)
                    }
                }
            }
        } else {
            signInAnonymously()
        }
    }

    private fun signInAnonymously() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = auth.signInAnonymously().await()
                _state.value = AuthState(
                    user = result.user,
                    isAnonymous = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.isAnonymous) {
                    val credential = EmailAuthProvider.getCredential(email, password)
                    val result = currentUser.linkWithCredential(credential).await()
                    // set display name in Firebase Auth
                    result.user?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                    )?.await()
                    _state.value = AuthState(
                        user = result.user,
                        isAnonymous = false,
                        displayName = name
                    )
                } else {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    result.user?.updateProfile(
                        UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                    )?.await()
                    _state.value = AuthState(
                        user = result.user,
                        isAnonymous = false,
                        displayName = name
                    )
                }
                friendsRepository.saveUserProfile(
                    name = name,
                    email = email,
                    streak = 0,
                    trophies = 0
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            try {
                // update Firebase Auth display name
                auth.currentUser?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                )?.await()
                // update Firestore
                friendsRepository.updateName(name)
                _state.value = _state.value.copy(displayName = name)
            } catch (_: Exception) { }
        }
    }
    fun logIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                // load name from Firestore
                val name = friendsRepository.getCurrentUserName() ?: user?.displayName
                _state.value = AuthState(
                    user = user,
                    isAnonymous = false,
                    displayName = name
                )
                friendsRepository.saveUserProfile(
                    name = name ?: "",
                    email = email,
                    streak = 0,
                    trophies = 0
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _state.value = AuthState()
        signInAnonymously()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}