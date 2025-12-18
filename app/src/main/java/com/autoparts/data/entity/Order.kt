package com.autoparts.data.entity

data class Order(
    val id: Int,
    val userId: Int,
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "", // ДОБАВЬТЕ ЭТО ПОЛЕ
    val totalAmount: Double,
    val status: String,
    val createdAt: String,
    val itemsJson: String,
    val deliveryType: String = "pickup",
    val paymentType: String = "cash",
    val deliveryAddress: String = "",
    val deliveryPhone: String = "",
    val comment: String = "",
    // Дополнительные детали
    val deliveryDate: String = "", // Планируемая дата доставки
    val trackingNumber: String = "", // Трек-номер для отслеживания
    val courierName: String = "", // Имя курьера
    val courierPhone: String = "", // Телефон курьера
    val estimatedDeliveryTime: String = "" // Ориентировочное время доставки
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

    fun getFormattedDelivery(): String {
        return when (deliveryType) {
            "pickup" -> "Самовывоз"
            "delivery" -> "Доставка"
            else -> deliveryType
        }
    }

    fun getFormattedPayment(): String {
        return when (paymentType) {
            "cash" -> "Наличные"
            "card" -> "Карта"
            "online" -> "Онлайн"
            else -> paymentType
        }
    }
}