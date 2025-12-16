package com.autoparts.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.autoparts.R
import com.autoparts.data.database.DatabaseHelper
import com.autoparts.databinding.FragmentRegisterBinding
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.InputFilter
import android.content.res.ColorStateList
import android.graphics.Color

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DatabaseHelper(requireContext())

        // Настраиваем маску телефона
        setupPhoneMask()

        binding.buttonRegister.setOnClickListener {
            performRegistration()
        }

        binding.textLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }



    private fun setupPhoneMask() {
        // Используем стандартный форматтер телефона для России
        val watcher = PhoneNumberFormattingTextWatcher("RU")
        binding.editTextPhone.addTextChangedListener(watcher)

        // Устанавливаем начальное значение +7
        binding.editTextPhone.setText("+7")
        binding.editTextPhone.setSelection(2)

        // Добавляем фильтр для ограничения длины
        binding.editTextPhone.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(18)) // +7 + 10 цифр

        // Добавляем валидацию и защиту от удаления +7
        binding.editTextPhone.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true
                val text = s?.toString() ?: ""

                // Удаляем все нецифровые символы, кроме +
                val digitsOnly = text.replace(Regex("[^\\d+]"), "")

                // Если текст не начинается с +7, исправляем это
                if (!digitsOnly.startsWith("+7")) {
                    val cleanText = digitsOnly.replace("+", "").replace("7", "")
                    val newText = "+7$cleanText"
                    binding.editTextPhone.removeTextChangedListener(this)
                    binding.editTextPhone.setText(newText)
                    binding.editTextPhone.setSelection(Math.min(newText.length, 12))
                    binding.editTextPhone.addTextChangedListener(this)
                }
                // Если введено слишком много цифр, обрезаем
                else if (digitsOnly.length > 12) {
                    val trimmedText = digitsOnly.substring(0, 12)
                    binding.editTextPhone.removeTextChangedListener(this)
                    binding.editTextPhone.setText(trimmedText)
                    binding.editTextPhone.setSelection(trimmedText.length)
                    binding.editTextPhone.addTextChangedListener(this)
                }

                // Можно добавить визуальную обратную связь
                updatePhoneValidationStatus(text)

                isFormatting = false
            }
        })
    }

    private fun updatePhoneValidationStatus(phoneText: String) {
        // Удаляем форматирование и проверяем длину
        val cleanDigits = phoneText.replace(Regex("[^\\d]"), "")
        val isValid = cleanDigits.length == 11 && cleanDigits.startsWith("7") // 7 + 10 цифр

        // Меняем цвет рамки или показываем подсказку
        binding.editTextPhone.backgroundTintList = ColorStateList.valueOf(
            if (isValid) Color.GREEN else Color.RED
        )
    }

    private fun performRegistration() {
        val name = binding.editTextName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

        // Проверка заполнения полей
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Введите корректный email", Toast.LENGTH_SHORT).show()
            binding.textInputLayoutEmail.error = "Некорректный email"
            return
        }

        // Проверка телефона (убираем все нецифровые символы кроме +)
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        if (cleanPhone.length < 12) { // +7XXXXXXXXXX
            Toast.makeText(requireContext(), "Введите корректный телефон", Toast.LENGTH_SHORT).show()
            binding.textInputLayoutPhone.error = "Некорректный телефон (пример: +7 999 123-45-67)"
            return
        }

        // Проверка пароля
        if (password.length < 6) {
            Toast.makeText(requireContext(), "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
            binding.textInputLayoutPassword.error = "Минимум 6 символов"
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            binding.textInputLayoutConfirmPassword.error = "Пароли не совпадают"
            return
        }

        // Проверка уникальности email
        if (dbHelper.isEmailExists(email)) {
            Toast.makeText(requireContext(), "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show()
            binding.textInputLayoutEmail.error = "Email уже используется"
            return
        }

        // Проверка уникальности телефона
        if (dbHelper.isPhoneExists(cleanPhone)) {
            Toast.makeText(requireContext(), "Пользователь с таким телефоном уже существует", Toast.LENGTH_SHORT).show()
            binding.textInputLayoutPhone.error = "Телефон уже используется"
            return
        }

        // Регистрация пользователя
        val userId = dbHelper.addUser(email, cleanPhone, password, name, "user")

        if (userId != -1L) {
            Toast.makeText(requireContext(), "Регистрация успешна!", Toast.LENGTH_SHORT).show()

            // Автоматически авторизуем пользователя после регистрации
            autoLoginAfterRegistration(email, password)
        } else {
            Toast.makeText(requireContext(), "Ошибка регистрации", Toast.LENGTH_SHORT).show()
        }
    }

    private fun autoLoginAfterRegistration(email: String, password: String) {
        // Пытаемся найти нового пользователя
        val user = dbHelper.getUserByEmail(email)

        if (user != null && dbHelper.checkUserPassword(user.id, password)) {
            // Сохраняем пользователя в SessionManager
            (requireActivity() as? com.autoparts.AuthActivity)?.let { authActivity ->
                val sessionManager = com.autoparts.SessionManager(authActivity)
                sessionManager.saveCurrentUser(user)

                Toast.makeText(requireContext(), "Автоматический вход выполнен", Toast.LENGTH_SHORT).show()
                authActivity.navigateToMain()
            }
        } else {
            // Если автоматический вход не удался, переходим на экран входа
            Toast.makeText(requireContext(), "Регистрация успешна. Теперь войдите", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}