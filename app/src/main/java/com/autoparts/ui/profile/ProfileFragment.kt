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
import com.autoparts.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        val user = sessionManager.getCurrentUser()
        if (user != null) {
            binding.userName.text = user.name
            binding.userEmail.text = user.email

            val formattedPhone = formatPhoneForDisplay(user.phone)
            binding.userPhone.text = formattedPhone
        } else {
            binding.userName.text = "Пользователь не найден"
            binding.userEmail.text = ""
            binding.userPhone.text = ""
        }
    }

    private fun formatPhoneForDisplay(phone: String): String {
        val digits = phone.filter { it.isDigit() }

        if (digits.length != 11) return phone

        return try {
            "+7 (${digits.substring(1, 4)}) ${digits.substring(4, 7)}-${digits.substring(7, 9)}-${digits.substring(9, 11)}"
        } catch (e: Exception) {
            phone
        }
    }

    private fun setupListeners() {
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.editProfileButton.setOnClickListener {
            // Навигация к редактированию профиля
            findNavController().navigate(R.id.action_navigation_profile_to_editProfileFragment)
        }
    }

    private fun showLogoutConfirmation() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Выход из системы")
            .setMessage("Вы уверены, что хотите выйти?")
            .setPositiveButton("Выйти") { _, _ ->
                sessionManager.logout()
                requireActivity().finish()
                Toast.makeText(requireContext(), "Вы вышли из системы", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}