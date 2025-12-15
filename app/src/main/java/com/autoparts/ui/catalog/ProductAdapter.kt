package com.autoparts.ui.catalog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.autoparts.R
import com.autoparts.CartManager
import com.autoparts.MainActivity
import com.autoparts.data.entity.Product

class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productBrand: TextView = itemView.findViewById(R.id.productBrand)
        val productArticle: TextView = itemView.findViewById(R.id.productArticle)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val addToCartButton: Button = itemView.findViewById(R.id.addToCartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        // Устанавливаем данные
        holder.productName.text = product.productsName
        holder.productBrand.text = product.brand
        holder.productArticle.text = "Арт: ${product.article}"
        holder.productPrice.text = "${product.price.toInt()} руб."

        // Кнопка добавления в корзину
        holder.addToCartButton.setOnClickListener {
            val cartManager = CartManager(holder.itemView.context)

            // Используем новый метод addProductToCart
            cartManager.addProductToCart(product, 1)

            Toast.makeText(
                holder.itemView.context,
                "Товар добавлен в корзину",
                Toast.LENGTH_SHORT
            ).show()

            // Обновляем бейдж корзины
            (holder.itemView.context as? MainActivity)?.refreshCartBadge()
        }

        // Клик на весь элемент для деталей
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}