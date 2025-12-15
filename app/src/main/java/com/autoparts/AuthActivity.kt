package com.autoparts

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.autoparts.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("=== AuthActivity.onCreate() ===")

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()

        // Проверяем авторизацию
        val sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            println("=== User already logged in, going to MainActivity ===")
            // НЕМЕДЛЕННЫЙ переход без задержки
            navigateToMain()
            return  // Важно: прерываем выполнение
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_auth) as NavHostFragment
        navController = navHostFragment.navController
    }

    fun navigateToMain() {
        println("=== AuthActivity.navigateToMain() called ===")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Закрываем AuthActivity
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("=== AuthActivity destroyed ===")
    }
}