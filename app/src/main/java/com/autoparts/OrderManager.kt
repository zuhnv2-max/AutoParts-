package com.autoparts

import android.content.Context
import android.util.Log
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.data.entity.Order
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OrderManager(context: Context) {

    private val dbHelper: DatabaseHelper = DatabaseHelper(context)
    private val gson = Gson()
    private val TAG = "OrderManager"

    data class OrderItem(
        val name: String,
        val quantity: Int,
        val price: Double,
        val total: Double
    )

    fun getUserOrders(userId: Int): List<Order> {
        return try {
            dbHelper.getOrdersByUserId(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения заказов: ${e.message}")
            emptyList()
        }
    }

    fun getAllOrders(): List<Order> {
        return try {
            dbHelper.getAllOrders()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения всех заказов: ${e.message}")
            emptyList()
        }
    }

    fun updateOrderStatus(orderId: Int, status: String): Boolean {
        return try {
            dbHelper.updateOrderStatus(orderId, status)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка обновления статуса: ${e.message}")
            false
        }
    }

    fun getOrderItems(order: Order): List<OrderItem> {
        return try {
            val type = object : TypeToken<List<OrderItem>>() {}.type
            gson.fromJson(order.itemsJson, type)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка парсинга товаров: ${e.message}")
            emptyList()
        }
    }

    // Новые методы для очистки заказов
    fun deleteAllOrders(): Boolean {
        return try {
            dbHelper.deleteAllOrders()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления всех заказов: ${e.message}")
            false
        }
    }

    fun deleteUserOrders(userId: Int): Boolean {
        return try {
            dbHelper.deleteUserOrders(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка удаления заказов пользователя: ${e.message}")
            false
        }
    }
}