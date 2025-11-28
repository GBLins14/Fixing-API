package com.fixing.api.utils

import org.springframework.stereotype.Component

@Component
class ValidatorUtil {
    private val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
    private val cpfRegex = "^\\d{11}$".toRegex()
    private val phoneRegex = "^\\d{10,11}$".toRegex()

    fun isValidEmail(email: String): Boolean {
        return emailRegex.matches(email)
    }

    fun cleanCpf(cpf: String): String {
        return cpf.replace(Regex("[^0-9]"), "")
    }

    fun isValidCpf(cpf: String): Boolean {
        val cleanedCpf = cleanCpf(cpf)
        if (!cpfRegex.matches(cleanedCpf)) return false

        // Check for known invalid CPFs
        if (cleanedCpf.all { it == cleanedCpf[0] }) return false

        // Validate CPF digits
        try {
            var d1 = 0
            var d2 = 0
            for (i in 0..8) {
                d1 += cleanedCpf[i].toString().toInt() * (10 - i)
            }

            d1 = 11 - (d1 % 11)
            if (d1 > 9) d1 = 0

            if (cleanedCpf[9].toString().toInt() != d1) return false

            for (i in 0..9) {
                d2 += cleanedCpf[i].toString().toInt() * (11 - i)
            }

            d2 = 11 - (d2 % 11)
            if (d2 > 9) d2 = 0

            return cleanedCpf[10].toString().toInt() == d2
        } catch (e: NumberFormatException) {
            return false
        }
    }

    fun isValidPhone(phone: String): Boolean {
        val cleanedPhone = phone.replace(Regex("[^0-9]"), "")
        return phoneRegex.matches(cleanedPhone)
    }
}
