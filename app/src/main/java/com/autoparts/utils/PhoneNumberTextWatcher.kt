package com.autoparts.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class PhoneNumberTextWatcher(private val editText: EditText) : TextWatcher {

    companion object {
        private const val MASK = "+7 (###) ###-##-##"
        private const val MASK_CHAR = '#'
        private const val DIGIT_PLACEHOLDER = '9'
    }

    private var isUpdating = false
    private var oldText = ""

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        oldText = s?.toString() ?: ""
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // Пустая реализация
    }

    override fun afterTextChanged(s: Editable?) {
        if (isUpdating) {
            return
        }

        val text = s?.toString() ?: ""
        if (text == oldText) {
            return
        }

        isUpdating = true

        try {
            // Очищаем текст от всех нецифровых символов
            val cleanText = text.filter { it.isDigit() }

            // Если текст пустой или начинается не с 7, 8 или +7
            if (cleanText.isEmpty() || (cleanText.length == 1 && cleanText != "7" && cleanText != "8")) {
                editText.setText("")
                editText.setSelection(0)
                isUpdating = false
                return
            }

            // Нормализуем номер: если начинается с 8, заменяем на 7
            var normalizedText = cleanText
            if (normalizedText.startsWith("8") && normalizedText.length > 1) {
                normalizedText = "7" + normalizedText.substring(1)
            }

            // Удаляем лишние цифры (больше 11)
            if (normalizedText.length > 11) {
                normalizedText = normalizedText.substring(0, 11)
            }

            // Применяем маску
            val maskedText = applyMask(normalizedText)

            // Устанавливаем отформатированный текст
            editText.setText(maskedText)

            // Устанавливаем курсор в конец
            val cursorPosition = maskedText.length
            editText.setSelection(cursorPosition)

        } finally {
            isUpdating = false
        }
    }

    private fun applyMask(cleanText: String): String {
        if (cleanText.isEmpty()) {
            return ""
        }

        // Удаляем первую цифру (7) для применения маски
        val digits = if (cleanText.startsWith("7") && cleanText.length > 1) {
            cleanText.substring(1)
        } else {
            cleanText
        }

        val masked = StringBuilder("+7 (")

        var digitIndex = 0

        // Проходим по маске и заменяем # на цифры
        for (i in 4 until MASK.length) {
            val maskChar = MASK[i]

            if (maskChar == MASK_CHAR) {
                if (digitIndex < digits.length) {
                    masked.append(digits[digitIndex])
                    digitIndex++
                } else {
                    // Если цифры закончились, ставим заглушку
                    masked.append(DIGIT_PLACEHOLDER)
                }
            } else {
                masked.append(maskChar)
            }

            // Если цифры закончились, добавляем оставшуюся часть маски
            if (digitIndex >= digits.length) {
                // Пропускаем оставшиеся цифровые позиции
                for (j in i + 1 until MASK.length) {
                    if (MASK[j] == MASK_CHAR) {
                        masked.append(DIGIT_PLACEHOLDER)
                    } else {
                        masked.append(MASK[j])
                    }
                }
                break
            }
        }

        return masked.toString()
    }

    // Метод для получения очищенного номера телефона
    fun getCleanPhoneNumber(): String {
        val text = editText.text?.toString() ?: ""
        val digits = text.filter { it.isDigit() }

        return if (digits.startsWith("7") || digits.startsWith("8")) {
            // Если номер начинается с 7 или 8, оставляем как есть
            digits
        } else if (digits.isNotEmpty()) {
            // Иначе добавляем 7 в начало
            "7$digits"
        } else {
            ""
        }
    }
}