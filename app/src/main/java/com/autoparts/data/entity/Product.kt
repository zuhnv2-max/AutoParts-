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
    val compatibleCars: String = ""
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