package com.autoparts.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoparts.R
import com.autoparts.MainActivity
import com.autoparts.SessionManager
import com.autoparts.CartManager
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.databinding.FragmentCartBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var cartManager: CartManager
    private lateinit var cartAdapter: CartAdapter
    private val gson = Gson()

    // Структура для товара в заказе
    data class OrderItem(
        val name: String,
        val quantity: Int,
        val price: Double,
        val total: Double
    )

    // Структура для товара в корзине
    data class CartItem(
        val productId: Int,
        val productName: String,
        val quantity: Int,
        val price: Double
    ) {
        fun getTotal(): Double = price * quantity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        dbHelper = DatabaseHelper(requireContext())
        cartManager = CartManager(requireContext())

        setupRecyclerView()
        loadCartItems()
        setupClickListeners()

        // Обновляем бейдж корзины
        (requireActivity() as? MainActivity)?.refreshCartBadge()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            cartItems = emptyList(),
            onQuantityChange = { productId, newQuantity ->
                updateCartItemQuantity(productId, newQuantity)
            },
            onRemoveClick = { productId ->
                removeFromCart(productId)
            }
        )

        val recyclerView = requireView().findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.cartRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = cartAdapter
    }

    private fun loadCartItems() {
        try {
            // Получаем товары из CartManager
            val cartItems = cartManager.getCartItems()

            // Обновляем адаптер
            cartAdapter.updateCartItems(cartItems)

            // Обновляем общую сумму и количество
            updateTotalAmount(cartItems)

            // Показываем/скрываем состояние пустой корзины
            updateEmptyState(cartItems.isEmpty())

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка загрузки корзины", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun updateTotalAmount(cartItems: List<CartItem>) {
        val total = cartItems.sumOf { it.getTotal() }
        val totalItems = cartItems.sumOf { it.quantity }

        // Общая сумма
        val totalTextView = requireView().findViewById<TextView>(R.id.totalPriceText)
        totalTextView.text = String.format("Итого: %.2f ₽", total)

        // Количество товаров
        val itemsTextView = requireView().findViewById<TextView>(R.id.totalItemsText)
        itemsTextView.text = "Товаров: $totalItems"

        // Кнопка оформления
        val checkoutButton = requireView().findViewById<Button>(R.id.checkoutButton)
        checkoutButton.isEnabled = cartItems.isNotEmpty()
        checkoutButton.alpha = if (cartItems.isNotEmpty()) 1.0f else 0.5f
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        val emptyState = requireView().findViewById<LinearLayout>(R.id.emptyState)
        val cartContent = requireView().findViewById<LinearLayout>(R.id.cartContent)

        if (isEmpty) {
            emptyState.visibility = View.VISIBLE
            cartContent.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            cartContent.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        // Кнопка оформления заказа
        val checkoutButton = requireView().findViewById<Button>(R.id.checkoutButton)
        checkoutButton.setOnClickListener {
            checkout()
        }

        // Кнопка очистки корзины
        val clearCartButton = requireView().findViewById<Button>(R.id.clearCartButton)
        clearCartButton.setOnClickListener {
            showClearCartConfirmation()
        }
    }

    private fun showClearCartConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Очистка корзины")
            .setMessage("Вы уверены, что хотите очистить корзину?")
            .setPositiveButton("Очистить") { _, _ ->
                clearCart()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearCart() {
        cartManager.clearCart()
        cartAdapter.updateCartItems(emptyList())
        updateTotalAmount(emptyList())
        updateEmptyState(true)

        (requireActivity() as? MainActivity)?.refreshCartBadge()

        Toast.makeText(requireContext(), "Корзина очищена", Toast.LENGTH_SHORT).show()
    }

    private fun checkout() {
        val user = sessionManager.getCurrentUser()
        if (user == null) {
            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
            return
        }

        // Показываем диалог с выбором доставки и оплаты
        showDeliveryAndPaymentDialog(user)
    }

    private fun showDeliveryAndPaymentDialog(user: com.autoparts.data.entity.User) {
        // Создаем кастомный диалог
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_checkout, null)

        // Находим элементы
        val deliveryGroup = dialogView.findViewById<RadioGroup>(R.id.deliveryGroup)
        val paymentGroup = dialogView.findViewById<RadioGroup>(R.id.paymentGroup)
        val addressInput = dialogView.findViewById<EditText>(R.id.addressInput)
        val phoneInput = dialogView.findViewById<EditText>(R.id.phoneInput)
        val commentInput = dialogView.findViewById<EditText>(R.id.commentInput)
        val pickupAddressContainer = dialogView.findViewById<LinearLayout>(R.id.pickupAddressContainer)
        val deliveryAddressContainer = dialogView.findViewById<LinearLayout>(R.id.deliveryAddressContainer)

        // Устанавливаем телефон пользователя по умолчанию
        val userPhone = sessionManager.getUserPhone()
        if (userPhone.isNotEmpty()) {
            phoneInput.setText(userPhone)
        }

        // Обработчик изменения способа доставки
        deliveryGroup.setOnCheckedChangeListener { group, checkedId ->
            val isDelivery = checkedId == R.id.radioDelivery

            if (isDelivery) {
                // Показываем поле для ввода адреса доставки
                pickupAddressContainer.visibility = View.GONE
                deliveryAddressContainer.visibility = View.VISIBLE
                addressInput.setText("") // Очищаем поле
            } else {
                // Показываем адрес магазина для самовывоза
                pickupAddressContainer.visibility = View.VISIBLE
                deliveryAddressContainer.visibility = View.GONE
                addressInput.setText("Самовывоз") // Устанавливаем значение по умолчанию
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Оформление заказа")
            .setView(dialogView)
            .setPositiveButton("Подтвердить заказ") { dialog, _ ->
                // Получаем данные из диалога
                val deliveryType = when (deliveryGroup.checkedRadioButtonId) {
                    R.id.radioPickup -> "pickup"
                    R.id.radioDelivery -> "delivery"
                    else -> "pickup"
                }

                val paymentType = when (paymentGroup.checkedRadioButtonId) {
                    R.id.radioCash -> "cash"
                    R.id.radioCard -> "card"
                    R.id.radioOnline -> "online"
                    else -> "cash"
                }

                val phone = phoneInput.text.toString().trim()
                val comment = commentInput.text.toString().trim()

                // Получаем адрес в зависимости от типа доставки
                val address = if (deliveryType == "delivery") {
                    addressInput.text.toString().trim()
                } else {
                    // Адрес магазина для самовывоза
                    "Самовывоз: ул. Автозапчастей, д. 15, Москва (ежедневно 9:00-21:00)"
                }

                // Проверяем обязательные поля
                if (phone.isEmpty()) {
                    Toast.makeText(requireContext(), "Введите телефон для связи", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (deliveryType == "delivery" && address.isEmpty()) {
                    Toast.makeText(requireContext(), "Введите адрес доставки", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Продолжаем оформление
                completeCheckout(user, deliveryType, paymentType, address, phone, comment)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun completeCheckout(
        user: com.autoparts.data.entity.User,
        deliveryType: String,
        paymentType: String,
        address: String,
        phone: String,
        comment: String = ""
    ) {
        try {
            // Получаем товары из корзины
            val cartItems = cartManager.getCartItems()

            if (cartItems.isEmpty()) {
                Toast.makeText(requireContext(), "Корзина пуста", Toast.LENGTH_SHORT).show()
                return
            }

            // Рассчитываем общую сумму
            val totalAmount = cartItems.sumOf { it.getTotal() }

            // Преобразуем товары в JSON
            val itemsJson = createItemsJson(cartItems)

            // Создаем детали заказа с комментарием
            val orderDetails = createOrderDetails(deliveryType, paymentType, address, phone, comment)

            // Создаем заказ в базе данных
            val orderId = dbHelper.createOrderWithDetails(
                userId = user.id,
                totalAmount = totalAmount,
                itemsJson = itemsJson,
                deliveryType = deliveryType,
                paymentType = paymentType,
                address = address,
                phone = phone,
                comment = comment
            )

            if (orderId != -1L) {
                // Очищаем корзину
                cartManager.clearCart()

                // Обновляем UI
                cartAdapter.updateCartItems(emptyList())
                updateTotalAmount(emptyList())
                updateEmptyState(true)

                // Обновляем бейдж
                (requireActivity() as? MainActivity)?.refreshCartBadge()

                // Показываем успешное оформление
                showOrderSuccessDialog(orderId, totalAmount, deliveryType, paymentType, address)

            } else {
                Toast.makeText(requireContext(), "Ошибка создания заказа", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun createItemsJson(cartItems: List<CartItem>): String {
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                name = cartItem.productName,
                quantity = cartItem.quantity,
                price = cartItem.price,
                total = cartItem.getTotal()
            )
        }
        return gson.toJson(orderItems)
    }

    private fun createOrderDetails(
        deliveryType: String,
        paymentType: String,
        address: String,
        phone: String,
        comment: String = ""
    ): String {
        val details = mapOf(
            "delivery" to mapOf(
                "type" to deliveryType,
                "address" to address,
                "phone" to phone
            ),
            "payment" to mapOf(
                "type" to paymentType
            ),
            "comment" to comment,
            "timestamp" to System.currentTimeMillis()
        )
        return gson.toJson(details)
    }

    private fun showOrderSuccessDialog(
        orderId: Long,
        totalAmount: Double,
        deliveryType: String,
        paymentType: String,
        address: String
    ) {
        val deliveryText = when (deliveryType) {
            "pickup" -> "Самовывоз"
            "delivery" -> "Доставка курьером"
            else -> deliveryType
        }

        val paymentText = when (paymentType) {
            "cash" -> "Наличными при получении"
            "card" -> "Картой при получении"
            "online" -> "Онлайн оплата"
            else -> paymentType
        }

        val message = """
        Заказ №$orderId успешно оформлен!
        
        Сумма: ${String.format("%.2f ₽", totalAmount)}
        $deliveryText
        Адрес: ${if (deliveryType == "pickup") "ул. Автозапчастей, д. 15, Москва" else address}
        Способ оплаты: $paymentText
        Статус: Ожидает обработки
    """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("✅ Заказ оформлен!")
            .setMessage(message)
            .setPositiveButton("Перейти к заказам") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(R.id.navigation_orders)
            }
            .setNegativeButton("Продолжить покупки") { dialog, _ ->
                dialog.dismiss()
                findNavController().navigate(R.id.navigation_catalog)
            }
            .setCancelable(false)
            .show()
    }

    private fun updateCartItemQuantity(productId: Int, newQuantity: Int) {
        if (newQuantity > 0) {
            cartManager.updateCartItem(productId, newQuantity)
            Toast.makeText(requireContext(), "Количество обновлено", Toast.LENGTH_SHORT).show()
        } else {
            cartManager.removeFromCart(productId)
            Toast.makeText(requireContext(), "Товар удален", Toast.LENGTH_SHORT).show()
        }

        loadCartItems()
        (requireActivity() as? MainActivity)?.refreshCartBadge()
    }

    private fun removeFromCart(productId: Int) {
        cartManager.removeFromCart(productId)

        loadCartItems()
        (requireActivity() as? MainActivity)?.refreshCartBadge()

        Toast.makeText(requireContext(), "Товар удален из корзины", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadCartItems()
        (requireActivity() as? MainActivity)?.refreshCartBadge()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}