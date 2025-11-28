package com.fixing.api.services

import com.fixing.api.enums.Role
import com.fixing.api.repositories.AccountRepository
import com.fixing.api.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class RequireAdminService(private val accountRepository: AccountRepository, private val jwtUtil: JwtUtil) {
    fun validateAdmin(authHeader: String?): ResponseEntity<Any>? {
        if (authHeader.isNullOrBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Header Authorization não enviado."))
        }

        val token = authHeader.removePrefix("Bearer ").trim()
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Token JWT vazio."))
        }

        val username = try {
            jwtUtil.getUsername(token)
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Token inválido ou expirado."))
        }

        val account = accountRepository.findByUsername(username)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Conta não encontrada."))

        if (account.role != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Você não é um administrador."))
        }

        return null
    }

}
