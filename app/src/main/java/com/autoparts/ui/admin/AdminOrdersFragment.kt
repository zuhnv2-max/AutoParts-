package com.autoparts.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoparts.R
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.data.entity.Order
import com.autoparts.databinding.FragmentAdminOrdersBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminOrdersFragment : Fragment() {

    private var _binding: FragmentAdminOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var orderAdapter: AdminOrderAdapter
    private val orderList = mutableListOf<Order>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())
        setupRecyclerView()
        loadOrders()

        binding.buttonRefresh.setOnClickListener {
            loadOrders()
            Toast.makeText(requireContext(), "Список обновлен", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = AdminOrderAdapter(orderList) { order ->
            showStatusDialog(order)
        }

        binding.recyclerViewOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewOrders.adapter = orderAdapter
    }

    private fun loadOrders() {
        try {
            orderList.clear()
            val orders = dbHelper.getAllOrders()
            orderList.addAll(orders)
            orderAdapter.notifyDataSetChanged()

            updateEmptyState()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка загрузки заказов: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showStatusDialog(order: Order) {
        val statuses = arrayOf(
            "Ожидает",
            "В обработке",
            "Отправлен",
            "Доставлен",
            "Отменен"
        )

        val currentStatusIndex = when (order.status) {
            "pending" -> 0
            "processing" -> 1
            "shipped" -> 2
            "delivered" -> 3
            "cancelled" -> 4
            else -> 0
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Изменить статус заказа #${order.id}")
            .setSingleChoiceItems(statuses, currentStatusIndex) { dialog, which ->
                val newStatus = when (which) {
                    0 -> "pending"
                    1 -> "processing"
                    2 -> "shipped"
                    3 -> "delivered"
                    4 -> "cancelled"
                    else -> "pending"
                }

                updateOrderStatus(order.id, newStatus)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateOrderStatus(orderId: Int, status: String) {
        val success = dbHelper.updateOrderStatus(orderId, status)
        if (success) {
            loadOrders()
            Toast.makeText(requireContext(), "Статус обновлен", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Ошибка обновления", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyState() {
        if (orderList.isEmpty()) {
            binding.textEmpty.visibility = View.VISIBLE
            binding.recyclerViewOrders.visibility = View.GONE
        } else {
            binding.textEmpty.visibility = View.GONE
            binding.recyclerViewOrders.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}