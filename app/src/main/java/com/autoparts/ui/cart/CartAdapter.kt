package com.autoparts.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.autoparts.R

class CartAdapter(
    private var cartItems: List<CartFragment.CartItem>,
    private val onQuantityChange: (Int, Int) -> Unit,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Используем ID из ВАШЕГО item_cart.xml
        val textProductName: TextView = itemView.findViewById(R.id.cartProductName)
        val textProductPrice: TextView = itemView.findViewById(R.id.cartProductPrice)
        val textQuantity: TextView = itemView.findViewById(R.id.cartQuantityText)
        val textTotalPrice: TextView = itemView.findViewById(R.id.cartTotalPrice)
        val buttonDecrease: Button = itemView.findViewById(R.id.cartDecreaseButton)
        val buttonIncrease: Button = itemView.findViewById(R.id.cartIncreaseButton)
        val buttonRemove: Button = itemView.findViewById(R.id.cartRemoveButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Используем ВАШ макет item_cart.xml
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }
    fun getCartItems(): List<CartFragment.CartItem> {
        return cartItems
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartItems[position]
        val total = cartItem.price * cartItem.quantity

        // Заполняем данные
        holder.textProductName.text = cartItem.productName
        holder.textProductPrice.text = "${String.format("%.2f ₽", cartItem.price)} за шт."
        holder.textQuantity.text = cartItem.quantity.toString()
        holder.textTotalPrice.text = String.format("%.2f ₽", total)

        // Обработчики кнопок
        holder.buttonDecrease.setOnClickListener {
            if (cartItem.quantity > 1) {
                onQuantityChange(cartItem.productId, cartItem.quantity - 1)
            }
        }

        holder.buttonIncrease.setOnClickListener {
            onQuantityChange(cartItem.productId, cartItem.quantity + 1)
        }

        holder.buttonRemove.setOnClickListener {
            onRemoveClick(cartItem.productId)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    fun updateCartItems(newCartItems: List<CartFragment.CartItem>) {
        cartItems = newCartItems
        notifyDataSetChanged()
    }
}