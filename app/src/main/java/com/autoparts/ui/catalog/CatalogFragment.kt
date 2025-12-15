package com.autoparts.ui.catalog

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.autoparts.databinding.FragmentCatalogBinding
import com.autoparts.CartManager
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.data.entity.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartManager: CartManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем CartManager и DatabaseHelper
        cartManager = CartManager(requireContext())
        dbHelper = DatabaseHelper(requireContext())

        setupRecyclerView()
        loadProducts()
        setupSearchListeners()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList()) { product ->
            // Обработка клика на товар (открытие деталей)
            showProductDetails(product)
        }

        // Используем правильные ID из вашего XML
        val recyclerView: RecyclerView = binding.productsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = productAdapter
    }

    private fun setupSearchListeners() {
        // Поиск (если будет реализован позже)
        binding.searchInput.setOnEditorActionListener { v, actionId, event ->
            // Обработка поиска
            false
        }

        binding.clearSearchButton.setOnClickListener {
            binding.searchInput.text.clear()
            binding.clearSearchButton.visibility = View.GONE
            loadProducts() // Загружаем все товары
        }

        // Показываем кнопку очистки при вводе текста
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Не используется
            }

            override fun afterTextChanged(s: Editable?) {
                binding.clearSearchButton.visibility =
                    if (s.isNullOrEmpty()) View.GONE else View.VISIBLE

                // Если поиск не пустой, выполняем поиск
                val query = s?.toString()?.trim()
                if (!query.isNullOrEmpty()) {
                    searchProducts(query)
                } else {
                    loadProducts() // Загружаем все товары
                }
            }
        })
    }

    private fun loadProducts() {
        lifecycleScope.launch {
            try {
                // Показываем прогресс бар
                binding.progressBar.visibility = View.VISIBLE

                val products = withContext(Dispatchers.IO) {
                    // Загружаем продукты из базы данных
                    dbHelper.getAllProducts()
                }

                if (products.isNotEmpty()) {
                    productAdapter.updateProducts(products)
                    binding.productsRecyclerView.visibility = View.VISIBLE
                } else {
                    // Можно добавить TextView для пустого состояния
                    Toast.makeText(requireContext(), "Товары не найдены", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки товаров: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun searchProducts(query: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val products = withContext(Dispatchers.IO) {
                    // Ищем товары в базе данных
                    dbHelper.searchProducts(query)
                }

                productAdapter.updateProducts(products)

                if (products.isEmpty()) {
                    Toast.makeText(requireContext(), "По запросу '$query' ничего не найдено", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка поиска", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showProductDetails(product: Product) {
        // Создаем диалог с деталями товара
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(product.productsName)
            .setMessage(
                """
                Бренд: ${product.brand}
                Артикул: ${product.article}
                Цена: ${product.price} руб.
                
                ${if (product.description.isNotEmpty()) "Описание: ${product.description}" else "Описание не указано"}
                
                ${if (product.compatibleCars.isNotEmpty()) "Подходит для: ${product.compatibleCars}" else ""}
                ${if (product.vinNumbers.isNotEmpty()) "VIN: ${product.vinNumbers}" else ""}
                """.trimIndent()
            )
            .setPositiveButton("Добавить в корзину") { dialog, _ ->
                // Добавляем товар в корзину через CartManager
                cartManager.addProductToCart(product, 1)
                Toast.makeText(requireContext(), "${product.productsName} добавлен в корзину", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Обновляем список при возвращении на фрагмент
        loadProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}