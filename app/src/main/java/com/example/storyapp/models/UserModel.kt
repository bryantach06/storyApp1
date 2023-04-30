package com.example.storyapp.models

data class UserModel(
    val name: String,
    val email: String,
    val password: String,
    val isLoggedIn: Boolean
)
