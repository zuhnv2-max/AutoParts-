package com.autoparts.ui.orders

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoparts.OrderManager
import com.autoparts.SessionManager
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.databinding.FragmentOrdersBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var orderManager: OrderManager
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var ordersAdapter: OrdersAdapter
    private var isAdmin: Boolean = false
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        orderManager = OrderManager(requireContext())
        dbHelper = DatabaseHelper(requireContext())
        isAdmin = sessionManager.isAdmin()

        setupRecyclerView()
        loadOrders()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(
            orders = emptyList(),
            isAdmin = isAdmin,
            onStatusChangeClick = { order ->
                if (isAdmin) {
                    showStatusChangeDialog(order)
                }
            },
            onOrderClick = { order ->
                showOrderDetails(order)
            }
        )

        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.ordersRecyclerView.adapter = ordersAdapter
    }

    private fun setupClickListeners() {
        // Обработчик кнопки очистки истории заказов
        binding.clearOrdersButton.setOnClickListener {
            showClearOrdersConfirmation()
        }

        // Дополнительные способы вызова очистки (по долгому нажатию)
        binding.ordersStats.setOnLongClickListener {
            showClearOrdersConfirmation()
            true
        }

        binding.emptyState.setOnLongClickListener {
            showClearOrdersConfirmation()
            true
        }
    }

    private fun loadOrders() {
        try {
            Log.d("ORDERS_DEBUG", "Загрузка заказов, isAdmin=$isAdmin")

            val orders = if (isAdmin) {
                // Админ видит ВСЕ заказы
                val allOrders = getAllOrders()
                Log.d("ORDERS_DEBUG", "Загружено всех заказов: ${allOrders.size}")
                allOrders
            } else {
                // Обычный пользователь видит только свои заказы
                val user = sessionManager.getCurrentUser()
                if (user != null) {
                    val userOrders = getUserOrders(user.id)
                    Log.d("ORDERS_DEBUG", "Загружено заказов пользователя ${user.id}: ${userOrders.size}")
                    userOrders
                } else {
                    Log.d("ORDERS_DEBUG", "Пользователь не найден")
                    emptyList()
                }
            }

            if (orders.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.ordersRecyclerView.visibility = View.GONE
                binding.clearOrdersButton.visibility = View.GONE
                Log.d("ORDERS_DEBUG", "Нет заказов для отображения")
            } else {
                binding.emptyState.visibility = View.GONE
                binding.ordersRecyclerView.visibility = View.VISIBLE
                binding.clearOrdersButton.visibility = View.VISIBLE
                ordersAdapter.updateOrders(orders)
                Log.d("ORDERS_DEBUG", "Отображено заказов: ${orders.size}")
            }

            updateOrdersStats(orders)

        } catch (e: Exception) {
            Log.e("ORDERS_DEBUG", "Ошибка загрузки заказов: ${e.message}")
            Toast.makeText(requireContext(), "Ошибка загрузки заказов", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun getAllOrders(): List<com.autoparts.data.entity.Order> {
        return try {
            dbHelper.getAllOrders()
        } catch (e: Exception) {
            Log.e("ORDERS_DEBUG", "Ошибка получения всех заказов: ${e.message}")
            emptyList()
        }
    }

    private fun getUserOrders(userId: Int): List<com.autoparts.data.entity.Order> {
        return try {
            dbHelper.getOrdersByUserId(userId)
        } catch (e: Exception) {
            Log.e("ORDERS_DEBUG", "Ошибка получения заказов пользователя: ${e.message}")
            emptyList()
        }
    }

    private fun updateOrdersStats(orders: List<com.autoparts.data.entity.Order>) {
        val totalOrders = orders.size
        val totalAmount = orders.sumOf { it.totalAmount }

        if (isAdmin) {
            val uniqueUsers = orders.map { it.userId }.distinct().size
            binding.ordersStats.text = "Заказов: $totalOrders • Пользователей: $uniqueUsers • Сумма: ${String.format("%.2f", totalAmount)} руб."
        } else {
            binding.ordersStats.text = "Заказов: $totalOrders • Потрачено: ${String.format("%.2f", totalAmount)} руб."
        }
    }

    private fun showClearOrdersConfirmation() {
        val user = sessionManager.getCurrentUser()
        if (user == null) {
            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
            return
        }

        val message = if (isAdmin) {
            "Вы действительно хотите удалить ВСЕ заказы всех пользователей?\n\n" +
                    "Это действие нельзя отменить. Все данные о заказах будут безвозвратно удалены."
        } else {
            "Вы действительно хотите очистить историю своих заказов?\n\n" +
                    "Это действие нельзя отменить. Все ваши заказы будут безвозвратно удалены."
        }

        val buttonText = if (isAdmin) "Удалить ВСЕ" else "Очистить"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Очистка истории заказов")
            .setMessage(message)
            .setPositiveButton(buttonText) { dialog, _ ->
                clearOrders()
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .setCancelable(true)
            .show()
    }

    private fun clearOrders() {
        val user = sessionManager.getCurrentUser()
        if (user == null) {
            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
            return
        }

        // Логируем начало операции
        Log.d("ORDERS_DEBUG", "Начало очистки заказов")
        Log.d("ORDERS_DEBUG", "isAdmin = $isAdmin")
        Log.d("ORDERS_DEBUG", "userId = ${user.id}")

        val success = try {
            if (isAdmin) {
                Log.d("ORDERS_DEBUG", "Вызов dbHelper.deleteAllOrders()")
                val result = dbHelper.deleteAllOrders()
                Log.d("ORDERS_DEBUG", "Результат deleteAllOrders: $result")
                result
            } else {
                Log.d("ORDERS_DEBUG", "Вызов dbHelper.deleteUserOrders(${user.id})")
                val result = dbHelper.deleteUserOrders(user.id)
                Log.d("ORDERS_DEBUG", "Результат deleteUserOrders: $result")
                result
            }
        } catch (e: Exception) {
            Log.e("ORDERS_DEBUG", "Ошибка при очистке: ${e.message}")
            e.printStackTrace()
            false
        }

        Log.d("ORDERS_DEBUG", "Итоговый результат очистки = $success")

        if (success) {
            Toast.makeText(requireContext(), "История заказов успешно очищена", Toast.LENGTH_SHORT).show()
            // Перезагружаем список заказов
            loadOrders()
        } else {
            Toast.makeText(requireContext(), "Не удалось очистить историю заказов", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStatusChangeDialog(order: com.autoparts.data.entity.Order) {
        val statuses = arrayOf("Ожидает", "В обработке", "Отправлен", "Доставлен", "Отменен")
        var selectedStatus = order.getFormattedStatus()
        val currentIndex = statuses.indexOfFirst { it == selectedStatus }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Изменить статус заказа №${order.id}")
            .setSingleChoiceItems(statuses, if (currentIndex >= 0) currentIndex else 0) { _, which ->
                selectedStatus = statuses[which]
            }
            .setPositiveButton("Сохранить") { dialog, _ ->
                val newStatus = when (selectedStatus) {
                    "Ожидает" -> "pending"
                    "В обработке" -> "processing"
                    "Отправлен" -> "shipped"
                    "Доставлен" -> "delivered"
                    "Отменен" -> "cancelled"
                    else -> "pending"
                }

                if (dbHelper.updateOrderStatus(order.id, newStatus)) {
                    Toast.makeText(requireContext(), "Статус обновлен", Toast.LENGTH_SHORT).show()
                    loadOrders()
                } else {
                    Toast.makeText(requireContext(), "Ошибка обновления", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showOrderDetails(order: com.autoparts.data.entity.Order) {
        try {
            val orderItems = getOrderItemsFromJson(order.itemsJson)
            val itemsText = orderItems.joinToString("\n") { item ->
                "• ${item.name} x${item.quantity} = ${String.format("%.2f руб.", item.total)}"
            }

            val details = StringBuilder()
            details.append("Заказ №${order.id}\n")

            if (isAdmin) {
                details.append("Покупатель: ${order.userName}\n")
                if (order.userEmail.isNotEmpty()) {
                    details.append("Email: ${order.userEmail}\n")
                }
                details.append("ID пользователя: ${order.userId}\n")
            }

            details.append("\n📅 Дата: ${order.getFormattedDate()}\n")
            details.append("💰 Сумма: ${String.format("%.2f руб.", order.totalAmount)}\n")
            details.append("📋 Статус: ${order.getFormattedStatus()}\n\n")
            details.append("🛒 Товары:\n$itemsText")

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Детали заказа")
                .setMessage(details.toString())
                .setPositiveButton("OK", null)
                .show()
        } catch (e: Exception) {
            Log.e("ORDERS_DEBUG", "Ошибка отображения деталей: ${e.message}")
            Toast.makeText(requireContext(), "Ошибка отображения деталей", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOrderItemsFromJson(itemsJson: String): List<OrderItem> {
        return try {
            val type = object : TypeToken<List<OrderItem>>() {}.type
            gson.fromJson(itemsJson, type)
        } catch (e: Exception) {
            Log.e("ORDERS_DEBUG", "Ошибка парсинга товаров: ${e.message}")
            emptyList()
        }
    }

    data class OrderItem(
        val name: String,
        val quantity: Int,
        val price: Double,
        val total: Double
    )

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}