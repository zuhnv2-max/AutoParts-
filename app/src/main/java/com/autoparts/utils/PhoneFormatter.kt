package com.autoparts.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class PhoneFormatter {

    companion object {
        private const val PHONE_MASK = "+7 (###) ###-##-##"
        private const val PHONE_PREFIX = "+7"
    }

    fun attachToEditText(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var isDeleting = false
            private var lastFormatted = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                isDeleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Nothing to do here
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                val cleanText = s?.toString()?.replace(Regex("[^0-9]"), "") ?: ""

                if (cleanText.isEmpty() || cleanText.length < 1) {
                    s?.clear()
                    s?.append(PHONE_PREFIX)
                } else if (cleanText.length == 1 && cleanText != "7") {
                    val formatted = formatPhoneNumber("7$cleanText")
                    s?.clear()
                    s?.append(formatted)
                } else if (cleanText.startsWith("7") || cleanText.startsWith("8")) {
                    val phoneWithoutPrefix = if (cleanText.startsWith("7")) {
                        cleanText.substring(1)
                    } else {
                        cleanText.substring(1)
                    }

                    val formatted = formatPhoneNumber("7$phoneWithoutPrefix")
                    s?.clear()
                    s?.append(formatted)
                } else {
                    val formatted = formatPhoneNumber("7$cleanText")
                    s?.clear()
                    s?.append(formatted)
                }

                lastFormatted = s?.toString() ?: ""
                isFormatting = false
            }
        })
    }

    fun getCleanPhoneNumber(phone: String): String {
        val clean = phone.replace(Regex("[^0-9]"), "")
        return if (clean.startsWith("7") || clean.startsWith("8")) {
            clean
        } else {
            "7$clean"
        }
    }

    private fun formatPhoneNumber(phone: String): String {
        val clean = phone.replace(Regex("[^0-9]"), "")

        if (clean.isEmpty()) return PHONE_PREFIX

        val phoneWithoutPrefix = if (clean.startsWith("7")) clean.substring(1) else clean

        if (phoneWithoutPrefix.isEmpty()) return PHONE_PREFIX

        val result = StringBuilder(PHONE_PREFIX)

        if (phoneWithoutPrefix.isNotEmpty()) {
            result.append(" (")

            for (i in phoneWithoutPrefix.indices) {
                if (i >= 10) break // Максимум 10 цифр после префикса

                when (i) {
                    0, 1, 2 -> result.append(phoneWithoutPrefix[i])
                    3 -> {
                        result.append(") ")
                        result.append(phoneWithoutPrefix[i])
                    }
                    4, 5 -> result.append(phoneWithoutPrefix[i])
                    6 -> {
                        result.append("-")
                        result.append(phoneWithoutPrefix[i])
                    }
                    7 -> result.append(phoneWithoutPrefix[i])
                    8 -> {
                        result.append("-")
                        result.append(phoneWithoutPrefix[i])
                    }
                    9 -> result.append(phoneWithoutPrefix[i])
                }
            }
        }

        return result.toString()
    }
}