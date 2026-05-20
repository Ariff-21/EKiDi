package com.example.ekidi.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val nama: String,
    val username: String,
    val password: String,
    val role: String,
    val umur: Int? = null
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: UserData? = null
)

data class UserData(
    val id: Int,
    val nama: String,
    val username: String,
    val role: String,
    val level: Int,
    val poin: Int,
    val token: String
)