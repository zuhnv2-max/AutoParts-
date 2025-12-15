package com.autoparts.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autoparts.R
import com.autoparts.data.entity.Order

class AdminOrderAdapter(
    private val orderList: List<Order>,
    private val onStatusClick: (Order) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textOrderId: TextView = itemView.findViewById(R.id.textOrderId)
        val textUserInfo: TextView = itemView.findViewById(R.id.textUserInfo)
        val textAmount: TextView = itemView.findViewById(R.id.textAmount)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textStatus: TextView = itemView.findViewById(R.id.textStatus)
        val buttonChangeStatus: Button = itemView.findViewById(R.id.buttonChangeStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]

        holder.textOrderId.text = "Заказ #${order.id}"
        holder.textUserInfo.text = "${order.userName} (${order.userEmail})"
        holder.textAmount.text = String.format("Сумма: %.2f ₽", order.totalAmount)
        holder.textDate.text = "Дата: ${order.getFormattedDate()}"
        holder.textStatus.text = order.getFormattedStatus()

        holder.buttonChangeStatus.setOnClickListener {
            onStatusClick(order)
        }
    }

    override fun getItemCount(): Int = orderList.size
}