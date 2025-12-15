package com.autoparts.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.autoparts.R
import com.autoparts.data.entity.Product

class AdminProductAdapter(
    private val productList: List<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        val textArticle: TextView = itemView.findViewById(R.id.textArticle)
        val textBrand: TextView = itemView.findViewById(R.id.textBrand)
        val textPrice: TextView = itemView.findViewById(R.id.textPrice)
        val textCategory: TextView = itemView.findViewById(R.id.textCategory)
        val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_admin_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.textProductName.text = product.productsName
        holder.textArticle.text = "Арт: ${product.article}"
        holder.textBrand.text = "Бренд: ${product.brand}"
        holder.textPrice.text = String.format("Цена: %.2f ₽", product.price)
        holder.textCategory.text = "Категория: ${product.category}"

        holder.buttonEdit.setOnClickListener {
            onEditClick(product)
        }

        holder.buttonDelete.setOnClickListener {
            onDeleteClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size
}