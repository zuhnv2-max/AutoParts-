package com.autoparts

import android.content.Context
import android.content.SharedPreferences
import com.autoparts.ui.cart.CartFragment.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addToCart(productId: Int, productName: String, price: Double, quantity: Int = 1) {
        val currentCart = getCartItems().toMutableList()

        // Проверяем, есть ли уже такой товар
        val existingItemIndex = currentCart.indexOfFirst { it.productId == productId }

        if (existingItemIndex != -1) {
            // Увеличиваем количество
            val existingItem = currentCart[existingItemIndex]
            currentCart[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
        } else {
            // Добавляем новый товар
            currentCart.add(CartItem(productId, productName, quantity, price))
        }

        saveCart(currentCart)
    }

    fun getCartItems(): List<CartItem> {
        val json = sharedPreferences.getString("cart_items", null)
        return if (json.isNullOrEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        }
    }

    fun getCartItemCount(): Int {
        return getCartItems().sumOf { it.quantity }
    }

    fun updateCartItem(productId: Int, newQuantity: Int) {
        val currentCart = getCartItems().toMutableList()
        val itemIndex = currentCart.indexOfFirst { it.productId == productId }

        if (itemIndex != -1) {
            if (newQuantity > 0) {
                currentCart[itemIndex] = currentCart[itemIndex].copy(quantity = newQuantity)
            } else {
                currentCart.removeAt(itemIndex)
            }
            saveCart(currentCart)
        }
    }

    fun removeFromCart(productId: Int) {
        val currentCart = getCartItems().toMutableList()
        currentCart.removeAll { it.productId == productId }
        saveCart(currentCart)
    }

    fun addProductToCart(product: com.autoparts.data.entity.Product, quantity: Int = 1) {
        addToCart(product.id, product.productsName, product.price, quantity)
    }

    fun clearCart() {
        sharedPreferences.edit().remove("cart_items").apply()
    }

    private fun saveCart(cartItems: List<CartItem>) {
        val json = gson.toJson(cartItems)
        sharedPreferences.edit().putString("cart_items", json).apply()
    }
}