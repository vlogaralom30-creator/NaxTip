package com.example.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.TikPromRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: TikPromRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    var isSignUp = MutableStateFlow(false)
    var email = MutableStateFlow("")
    var password = MutableStateFlow("")
    var username = MutableStateFlow("")
    var fullName = MutableStateFlow("")

    fun setSignUpMode(signUp: Boolean) {
        isSignUp.value = signUp
        _authState.value = AuthState.Idle
    }

    fun toggleMode() {
        isSignUp.value = !isSignUp.value
        _authState.value = AuthState.Idle
    }

    fun submit(onSuccess: () -> Unit) {
        val em = email.value.trim()
        val pass = password.value.trim()

        if (em.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }

        if (pass.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (isSignUp.value) {
                val uName = username.value.trim().ifEmpty { em.substringBefore("@") }
                val fName = fullName.value.trim().ifEmpty { uName }
                val result = repository.signUp(em, pass, uName, fName)
                result.fold(
                    onSuccess = { user ->
                        _authState.value = AuthState.Success(user)
                        onSuccess()
                    },
                    onFailure = { err ->
                        _authState.value = AuthState.Error(err.message ?: "Signup failed. Please try again.")
                    }
                )
            } else {
                val result = repository.signIn(em, pass)
                result.fold(
                    onSuccess = { user ->
                        _authState.value = AuthState.Success(user)
                        onSuccess()
                    },
                    onFailure = { err ->
                        _authState.value = AuthState.Error(err.message ?: "Invalid email or password.")
                    }
                )
            }
        }
    }

    fun continueAsGuest(onSuccess: () -> Unit) {
        // Leave user unauthenticated (Guest mode)
        _authState.value = AuthState.Idle
        onSuccess()
    }
}
