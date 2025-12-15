package com.autoparts.data.entity

data class Order(
    val id: Int,
    val userId: Int,
    val userName: String = "",
    val userEmail: String = "",
    val totalAmount: Double,
    val status: String,
    val createdAt: String,
    val itemsJson: String
) {
    fun getFormattedStatus(): String {
        return when (status) {
            "pending" -> "Ожидает"
            "processing" -> "В обработке"
            "shipped" -> "Отправлен"
            "delivered" -> "Доставлен"
            "cancelled" -> "Отменен"
            else -> status
        }
    }

    fun getFormattedDate(): String {
        return try {
            createdAt.substring(0, 10)
        } catch (e: Exception) {
            createdAt
        }
    }
}