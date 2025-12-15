package com.autoparts.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.autoparts.R
import com.autoparts.SessionManager
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        dbHelper = DatabaseHelper(requireContext())

        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        val user = sessionManager.getCurrentUser()

        if (user != null) {
            binding.editTextEmail.setText(user.email)
            binding.editTextPhone.setText(user.phone)
            binding.editTextName.setText(user.name)
            // Поля пароля оставляем пустыми
            binding.editTextCurrentPassword.setText("")
            binding.editTextNewPassword.setText("")
            binding.editTextConfirmPassword.setText("")
        } else {
            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        binding.buttonSave.setOnClickListener {
            saveProfile()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveProfile() {
        val email = binding.editTextEmail.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val name = binding.editTextName.text.toString().trim()
        val currentPassword = binding.editTextCurrentPassword.text.toString().trim()
        val newPassword = binding.editTextNewPassword.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

        // Валидация основных полей
        if (email.isEmpty() || phone.isEmpty() || name.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните Email, телефон и имя", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Введите корректный Email", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.length < 10) {
            Toast.makeText(requireContext(), "Введите корректный телефон (минимум 10 цифр)", Toast.LENGTH_SHORT).show()
            return
        }

        val user = sessionManager.getCurrentUser()
        if (user == null) {
            Toast.makeText(requireContext(), "Пользователь не найден", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем, нужно ли менять пароль
        val isChangingPassword = currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()

        if (isChangingPassword) {
            // Если хоть одно поле пароля заполнено - проверяем все
            if (currentPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Введите текущий пароль", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Введите новый пароль", Toast.LENGTH_SHORT).show()
                return
            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Подтвердите новый пароль", Toast.LENGTH_SHORT).show()
                return
            }

            // Проверяем текущий пароль
            val currentUser = dbHelper.checkUserCredentials(user.email, currentPassword)
            if (currentUser == null) {
                Toast.makeText(requireContext(), "Неверный текущий пароль", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Новый пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
                return
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Новые пароли не совпадают", Toast.LENGTH_SHORT).show()
                return
            }

            // Обновляем с новым паролем
            val success = updateUserWithPassword(
                userId = user.id,
                email = email,
                phone = phone,
                name = name,
                password = newPassword
            )

            if (success) {
                // Обновляем сессию
                val updatedUser = user.copy(
                    email = email,
                    phone = phone,
                    name = name,
                    password = newPassword
                )
                sessionManager.saveCurrentUser(updatedUser)
                Toast.makeText(requireContext(), "Профиль и пароль обновлены", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Ошибка обновления профиля", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Обновляем только профиль (без смены пароля)
            val success = updateUser(
                userId = user.id,
                email = email,
                phone = phone,
                name = name
            )

            if (success) {
                // Обновляем сессию
                val updatedUser = user.copy(
                    email = email,
                    phone = phone,
                    name = name
                )
                sessionManager.saveCurrentUser(updatedUser)
                Toast.makeText(requireContext(), "Профиль обновлен", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Ошибка обновления профиля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUser(userId: Int, email: String, phone: String, name: String): Boolean {
        return try {
            dbHelper.updateUser(userId, email, phone, name)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка базы данных: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun updateUserWithPassword(userId: Int, email: String, phone: String, name: String, password: String): Boolean {
        return try {
            // Пробуем вызвать метод updateUserWithPassword
            dbHelper::class.java.getMethod(
                "updateUserWithPassword",
                Int::class.java, String::class.java, String::class.java, String::class.java, String::class.java
            ).invoke(dbHelper, userId, email, phone, name, password) as Boolean
        } catch (e: NoSuchMethodException) {
            // Если метода нет, используем обычное обновление
            // (В реальном приложении нужно добавить метод в DatabaseHelper)
            Toast.makeText(requireContext(), "Метод обновления пароля не реализован", Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}