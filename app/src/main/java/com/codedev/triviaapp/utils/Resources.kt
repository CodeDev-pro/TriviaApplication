package com.codedev.triviaapp.utils

import android.util.Patterns
import com.google.firebase.auth.FirebaseUser
import java.util.regex.Pattern

sealed class LoginUiState {
    object Loading : LoginUiState()
    data class Success(val user: FirebaseUser) : LoginUiState()
    data class Error(val message: String): LoginUiState()
    object InitialState : LoginUiState()
}

sealed class SignUpUiState {
    object Loading : SignUpUiState()
    data class Success(val user: FirebaseUser) : SignUpUiState()
    data class Error(val message: String): SignUpUiState()
    object InitialState : SignUpUiState()
}
