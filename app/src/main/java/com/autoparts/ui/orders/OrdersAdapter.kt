package com.autoparts.ui.orders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autoparts.R
import com.autoparts.data.entity.Order

class OrdersAdapter(
    private var orders: List<Order>,
    private val isAdmin: Boolean,
    private val onStatusChangeClick: (Order) -> Unit,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Используем ваши ID
        val orderNumber: TextView = itemView.findViewById(R.id.orderNumber)
        val orderDate: TextView = itemView.findViewById(R.id.orderDate)
        val orderStatus: TextView = itemView.findViewById(R.id.orderStatus)
        val orderTotal: TextView = itemView.findViewById(R.id.orderTotal)
        val orderItemsCount: TextView = itemView.findViewById(R.id.orderItemsCount)
        val viewDetailsButton: Button = itemView.findViewById(R.id.viewDetailsButton)
        val changeStatusButton: Button = itemView.findViewById(R.id.changeStatusButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val orderItems = try {
            com.google.gson.Gson().fromJson<List<OrderItem>>(
                order.itemsJson,
                object : com.google.gson.reflect.TypeToken<List<OrderItem>>() {}.type
            )
        } catch (e: Exception) {
            emptyList<OrderItem>()
        }

        // Устанавливаем данные
        holder.orderNumber.text = "Заказ №${order.id}"
        holder.orderDate.text = order.getFormattedDate()
        holder.orderStatus.text = order.getFormattedStatus()
        holder.orderTotal.text = String.format("%.2f руб.", order.totalAmount)
        holder.orderItemsCount.text = "Товаров: ${orderItems.size}"

        // Настраиваем цвета статуса
        val context = holder.itemView.context
        when (order.status) {
            "pending" -> holder.orderStatus.setTextColor(
                context.resources.getColor(android.R.color.holo_orange_dark, context.theme)
            )
            "processing" -> holder.orderStatus.setTextColor(
                context.resources.getColor(android.R.color.holo_blue_dark, context.theme)
            )
            "shipped" -> holder.orderStatus.setTextColor(
                context.resources.getColor(android.R.color.holo_purple, context.theme)
            )
            "delivered" -> holder.orderStatus.setTextColor(
                context.resources.getColor(android.R.color.holo_green_dark, context.theme)
            )
            "cancelled" -> holder.orderStatus.setTextColor(
                context.resources.getColor(android.R.color.holo_red_dark, context.theme)
            )
        }

        // Для админа показываем кнопку изменения статуса
        if (isAdmin) {
            holder.changeStatusButton.visibility = View.VISIBLE
            holder.changeStatusButton.setOnClickListener {
                onStatusChangeClick(order)
            }
        } else {
            holder.changeStatusButton.visibility = View.GONE
        }

        // Кнопка просмотра деталей
        holder.viewDetailsButton.setOnClickListener {
            onOrderClick(order)
        }

        // Клик на всей карточке тоже показывает детали
        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orders.size

    fun updateOrders(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    fun getOrders(): List<Order> {
        return orders
    }

    // Вспомогательный класс для парсинга товаров заказа
    data class OrderItem(
        val name: String,
        val quantity: Int,
        val price: Double,
        val total: Double
    )
}