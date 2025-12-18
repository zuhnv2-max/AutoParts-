package com.autoparts.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoparts.AuthActivity
import com.autoparts.R
import com.autoparts.SessionManager
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.data.entity.Product
import com.autoparts.databinding.FragmentAdminSimpleBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminSimpleBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: AdminProductAdapter
    private lateinit var dbHelper: DatabaseHelper
    private val productList = mutableListOf<Product>()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminSimpleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            dbHelper = DatabaseHelper(requireContext())
            sessionManager = SessionManager(requireContext())
            setupRecyclerView()
            loadProductsFromDatabase()
            setupClickListeners()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка инициализации: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = AdminProductAdapter(
            productList = productList,
            onEditClick = { product ->
                showEditProductDialog(product)
            },
            onDeleteClick = { product ->
                showDeleteConfirmationDialog(product)
            }
        )

        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = productAdapter
    }

    private fun loadProductsFromDatabase() {
        try {
            productList.clear()
            val productsFromDb = dbHelper.getAllProducts()
            productList.addAll(productsFromDb)
            productAdapter.notifyDataSetChanged()

            updateEmptyState()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка загрузки товаров: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupClickListeners() {
        binding.buttonAddProduct.setOnClickListener {
            showAddProductDialog()
        }

        binding.buttonRefresh.setOnClickListener {
            refreshProductList()
        }

        binding.buttonClearAll.setOnClickListener {
            showClearAllConfirmationDialog()
        }

        binding.buttonLogoutAdmin.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выход из системы")
            .setMessage("Вы уверены, что хотите выйти из аккаунта администратора?")
            .setPositiveButton("Выйти") { _, _ ->
                sessionManager.logout()

                val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)

                Toast.makeText(requireContext(), "Вы вышли из системы", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_product, null)

        val etName = dialogView.findViewById<EditText>(R.id.etProductName)
        val etArticle = dialogView.findViewById<EditText>(R.id.etArticle)
        val etBrand = dialogView.findViewById<EditText>(R.id.etBrand)
        val etPrice = dialogView.findViewById<EditText>(R.id.etPrice)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etCategory = dialogView.findViewById<EditText>(R.id.etCategory)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Добавить товар")
            .setView(dialogView)
            .setPositiveButton("Добавить") { dialog, _ ->
                try {
                    val name = etName.text.toString().trim()
                    val article = etArticle.text.toString().trim()
                    val brand = etBrand.text.toString().trim()
                    val priceStr = etPrice.text.toString().trim()
                    val description = etDescription.text.toString().trim()
                    val category = etCategory.text.toString().trim()

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Введите название товара", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (article.isEmpty()) {
                        Toast.makeText(requireContext(), "Введите артикул", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (brand.isEmpty()) {
                        Toast.makeText(requireContext(), "Введите бренд", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val price = priceStr.toDoubleOrNull()
                    if (price == null || price <= 0) {
                        Toast.makeText(requireContext(), "Введите корректную цену", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val newProduct = Product(
                        id = 0,
                        productsName = name,
                        article = article,
                        brand = brand,
                        price = price,
                        description = description,
                        category = category,
                        imageUrl = "",
                        vinNumbers = "",
                        compatibleCars = ""
                    )

                    val result = dbHelper.addProduct(newProduct)
                    if (result != -1L) {
                        loadProductsFromDatabase()
                        Toast.makeText(requireContext(), "Товар успешно добавлен", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Ошибка при добавлении товара", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditProductDialog(product: Product) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_admin_product, null)

        val etName = dialogView.findViewById<EditText>(R.id.etProductName)
        val etArticle = dialogView.findViewById<EditText>(R.id.etArticle)
        val etBrand = dialogView.findViewById<EditText>(R.id.etBrand)
        val etPrice = dialogView.findViewById<EditText>(R.id.etPrice)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val etCategory = dialogView.findViewById<EditText>(R.id.etCategory)

        etName.setText(product.productsName)
        etArticle.setText(product.article)
        etBrand.setText(product.brand)
        etPrice.setText(product.price.toString())
        etDescription.setText(product.description)
        etCategory.setText(product.category)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Редактировать товар")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { dialog, _ ->
                try {
                    val name = etName.text.toString().trim()
                    val article = etArticle.text.toString().trim()
                    val brand = etBrand.text.toString().trim()
                    val priceStr = etPrice.text.toString().trim()
                    val description = etDescription.text.toString().trim()
                    val category = etCategory.text.toString().trim()

                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Введите название товара", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (article.isEmpty()) {
                        Toast.makeText(requireContext(), "Введите артикул", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    if (brand.isEmpty()) {
                        Toast.makeText(requireContext(), "Введите бренд", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val price = priceStr.toDoubleOrNull()
                    if (price == null || price <= 0) {
                        Toast.makeText(requireContext(), "Введите корректную цену", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val updatedProduct = product.copy(
                        productsName = name,
                        article = article,
                        brand = brand,
                        price = price,
                        description = description,
                        category = category
                    )

                    val rowsAffected = dbHelper.updateProduct(updatedProduct)
                    if (rowsAffected > 0) {
                        loadProductsFromDatabase()
                        Toast.makeText(requireContext(), "Товар успешно обновлен", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Ошибка при обновлении товара", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удаление товара")
            .setMessage("Вы уверены, что хотите удалить товар \"${product.productsName}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                deleteProductFromDatabase(product.id)
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteProductFromDatabase(productId: Int) {
        try {
            val rowsDeleted = dbHelper.deleteProduct(productId)
            if (rowsDeleted > 0) {
                val index = productList.indexOfFirst { it.id == productId }
                if (index != -1) {
                    productList.removeAt(index)
                    productAdapter.notifyItemRemoved(index)
                }
                updateEmptyState()
                Toast.makeText(requireContext(), "Товар успешно удален", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Ошибка при удалении товара", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showClearAllConfirmationDialog() {
        if (productList.isEmpty()) {
            Toast.makeText(requireContext(), "Список товаров пуст", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Очистка списка товаров")
            .setMessage("Вы уверены, что хотите удалить все товары (${productList.size} шт.)?\nЭто действие нельзя отменить.")
            .setPositiveButton("Очистить все") { _, _ ->
                clearAllProductsFromDatabase()
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun clearAllProductsFromDatabase() {
        try {
            productList.forEach { product ->
                dbHelper.deleteProduct(product.id)
            }

            val itemCount = productList.size
            productList.clear()
            productAdapter.notifyItemRangeRemoved(0, itemCount)

            updateEmptyState()
            Toast.makeText(requireContext(), "Все товары удалены (${itemCount} шт.)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshProductList() {
        loadProductsFromDatabase()
        Toast.makeText(requireContext(), "Список товаров обновлен", Toast.LENGTH_SHORT).show()
    }

    private fun updateEmptyState() {
        if (productList.isEmpty()) {
            binding.textEmpty.visibility = View.VISIBLE
            binding.recyclerViewProducts.visibility = View.GONE
        } else {
            binding.textEmpty.visibility = View.GONE
            binding.recyclerViewProducts.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        loadProductsFromDatabase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}