package com.codedev.triviaapp.models

data class User(
    val id: String? = null,
    val email: String,
    val displayName: String,
    val password: String
)