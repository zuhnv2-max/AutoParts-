package com.autoparts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.autoparts.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var cartManager: CartManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация
        cartManager = CartManager(this)
        sessionManager = SessionManager(this)

        // Настройка Navigation
        val navView: BottomNavigationView = binding.navView

        // Находим NavController
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Убираем ActionBar (ваш макет его не поддерживает)
        supportActionBar?.hide()

        // Создаем AppBarConfiguration
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_catalog,
                R.id.navigation_cart,
                R.id.navigation_orders,
                R.id.navigation_profile
            )
        )

        // Связываем BottomNavigationView с NavController
        navView.setupWithNavController(navController)

        // Показываем/скрываем админ-панель в зависимости от роли
        setupAdminMenuVisibility(navView)

        // Обновляем бейдж корзины
        refreshCartBadge()
    }

    private fun setupAdminMenuVisibility(navView: BottomNavigationView) {
        // Находим меню
        val menu = navView.menu
        val adminMenuItem = menu.findItem(R.id.navigation_admin)

        // Показываем админ-панель только для администраторов
        adminMenuItem.isVisible = sessionManager.isAdmin()

        // Если админ, добавляем админ-панель в AppBarConfiguration
        if (sessionManager.isAdmin()) {
            // Обновляем AppBarConfiguration с админ-панелью
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_catalog,
                    R.id.navigation_cart,
                    R.id.navigation_orders,
                    R.id.navigation_profile,
                    R.id.navigation_admin
                )
            )
            // Можно обновить навигацию, если нужно
        }
    }

    fun refreshCartBadge() {
        val itemCount = cartManager.getCartItemCount()
        val navView: BottomNavigationView = binding.navView

        if (itemCount > 0) {
            // Создаем или обновляем бейдж
            val badge = navView.getOrCreateBadge(R.id.navigation_cart)
            badge.number = itemCount
            badge.backgroundColor = getColor(android.R.color.holo_red_light)
            badge.badgeTextColor = getColor(android.R.color.white)
            badge.isVisible = true
        } else {
            // Удаляем бейдж
            try {
                navView.removeBadge(R.id.navigation_cart)
            } catch (e: Exception) {
                // Бейдж не существует
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCartBadge()
        // При возвращении в приложение обновляем видимость админ-меню
        setupAdminMenuVisibility(binding.navView)
    }

    fun getCartManager(): CartManager {
        return cartManager
    }
}