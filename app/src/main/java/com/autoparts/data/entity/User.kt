package com.autoparts.data.entity

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String = "user" // "user" или "admin"
)