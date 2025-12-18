package com.autoparts.data.entity

data class Product(
    val id: Int,
    val productsName: String,
    val article: String,
    val brand: String,
    val price: Double,
    val description: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val vinNumbers: String = "",
    val compatibleCars: String = "",
    // Дополнительные детали
    val stock: Int = 0, // Количество на складе
    val warranty: String = "", // Гарантия (например, "12 месяцев")
    val country: String = "", // Страна производства
    val weight: Double = 0.0, // Вес в кг
    val dimensions: String = "", // Размеры (например, "10x20x5 см")
    val rating: Double = 0.0, // Рейтинг (0-5)
    val reviewsCount: Int = 0, // Количество отзывов
    val createdAt: String = "" // Дата добавления
) {
    fun getFormattedCompatibleCars(): List<String> {
        return try {
            if (compatibleCars.isNotEmpty()) {
                compatibleCars.split(",").map { it.trim() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getFormattedVinNumbers(): List<String> {
        return try {
            if (vinNumbers.isNotEmpty()) {
                vinNumbers.split(",").map { it.trim() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}