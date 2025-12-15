package com.autoparts.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.autoparts.R
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        binding.buttonLogin.setOnClickListener {
            performLogin()
        }

        binding.textRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun performLogin() {
        val emailOrPhone = binding.editTextEmailOrPhone.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (emailOrPhone.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()

            if (emailOrPhone.isEmpty()) {
                binding.textInputLayoutEmailOrPhone.error = "Введите email или телефон"
            }
            if (password.isEmpty()) {
                binding.textInputLayoutPassword.error = "Введите пароль"
            }
            return
        }

        // Очищаем ошибки
        binding.textInputLayoutEmailOrPhone.error = null
        binding.textInputLayoutPassword.error = null

        // Проверяем тестового администратора
        if (emailOrPhone == "admin@test.com" && password == "admin123") {
            val adminUser = com.autoparts.data.entity.User(
                id = 1,
                name = "Администратор",
                email = "admin@test.com",
                phone = "+79001234567",
                password = "admin123",
                role = "admin"
            )

            val sessionManager = com.autoparts.SessionManager(requireContext())
            sessionManager.saveCurrentUser(adminUser)

            Toast.makeText(requireContext(), "Вход выполнен как администратор", Toast.LENGTH_SHORT).show()
            (requireActivity() as? com.autoparts.AuthActivity)?.navigateToMain()
            return
        }

        // Проверяем тестового пользователя
        if (emailOrPhone == "user@test.com" && password == "user123") {
            val user = com.autoparts.data.entity.User(
                id = 2,
                name = "Пользователь",
                email = "user@test.com",
                phone = "+79007654321",
                password = "user123",
                role = "user"
            )

            val sessionManager = com.autoparts.SessionManager(requireContext())
            sessionManager.saveCurrentUser(user)

            Toast.makeText(requireContext(), "Вход выполнен", Toast.LENGTH_SHORT).show()
            (requireActivity() as? com.autoparts.AuthActivity)?.navigateToMain()
            return
        }

        // Проверка в базе данных (по email или телефону)
        // Если введен телефон, приводим к формату +7XXXXXXXXXX
        val loginInput = if (emailOrPhone.contains("@")) {
            emailOrPhone
        } else {
            // Убираем все нецифровые символы кроме +
            val cleanPhone = emailOrPhone.replace(Regex("[^0-9+]"), "")
            if (cleanPhone.startsWith("7") && cleanPhone.length == 11) {
                "+$cleanPhone"
            } else if (cleanPhone.startsWith("8") && cleanPhone.length == 11) {
                "+7${cleanPhone.substring(1)}"
            } else if (cleanPhone.startsWith("+7") && cleanPhone.length == 12) {
                cleanPhone
            } else {
                emailOrPhone // Оставляем как есть, если не телефон
            }
        }

        val user = dbHelper.checkUserCredentials(loginInput, password)

        if (user != null) {
            val sessionManager = com.autoparts.SessionManager(requireContext())
            sessionManager.saveCurrentUser(user)

            Toast.makeText(requireContext(), "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
            (requireActivity() as? com.autoparts.AuthActivity)?.navigateToMain()
        } else {
            Toast.makeText(requireContext(), "Неверный email/телефон или пароль", Toast.LENGTH_SHORT).show()
            binding.textInputLayoutEmailOrPhone.error = "Неверные данные"
            binding.textInputLayoutPassword.error = "Неверные данные"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}