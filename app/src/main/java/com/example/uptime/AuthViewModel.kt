package com.example.uptime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val user: FirebaseUser? = null,
    val isAnonymous: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        // sign in anonymously if no user exists
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _state.value = AuthState(
                user = currentUser,
                isAnonymous = currentUser.isAnonymous
            )
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

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.isAnonymous) {
                    // link anonymous account to email/password
                    val credential = EmailAuthProvider.getCredential(email, password)
                    val result = currentUser.linkWithCredential(credential).await()
                    _state.value = AuthState(
                        user = result.user,
                        isAnonymous = false
                    )
                } else {
                    // create new account
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    _state.value = AuthState(
                        user = result.user,
                        isAnonymous = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun logIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _state.value = AuthState(
                    user = result.user,
                    isAnonymous = false
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
