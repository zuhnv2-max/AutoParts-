package com.autoparts.data.entity

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String = "user", // "user" или "admin"
    // Дополнительные детали
    val address: String = "", // Адрес пользователя
    val createdAt: String = "", // Дата регистрации
    val lastLoginAt: String = "", // Дата последнего входа
    val avatarUrl: String = "" // URL аватара
)