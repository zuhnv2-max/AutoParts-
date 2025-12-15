package com.autoparts.data.entity

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    fun getTotalPrice(): Double = product.price * quantity
}