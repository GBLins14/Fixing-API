package com.fixing.api.controllers

import com.fixing.api.models.CustomUserDetails
import com.fixing.api.models.Plan
import com.fixing.api.models.Role
import com.fixing.api.repositories.UserRepository
import com.fixing.api.schemas.BanAccountSchema
import com.fixing.api.schemas.UserSetRoleSchema
import com.fixing.api.schemas.UserSetPlanSchema
import com.fixing.api.services.RequireAdminService
import com.fixing.api.security.Hash
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/admin")
class AdminAccountController(private val userRepository: UserRepository, private val requireAdminService: RequireAdminService, private val bcrypt: Hash) {

    @GetMapping(value = ["/accounts", "/accounts/{login}"])
    fun getAccount(@PathVariable(required = false) login: String?): ResponseEntity<Any> {
        return if (login != null) {
            val account = userRepository.findByUsernameOrEmail(login, login)
            return if (account != null) {
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "account" to account
                ))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Conta não encontrada."))
            }
        } else {
            val accounts = userRepository.findAll()
            ResponseEntity.ok(mapOf(
                "success" to true,
                "account" to accounts
            ))
        }
    }

    @PatchMapping("/accounts/role")
    fun setRole(@RequestBody request: UserSetRoleSchema): ResponseEntity<Any> {
        val accountReq = userRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Conta não encontrada."))

        if (accountReq.role == request.role) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("success" to false, "message" to "A conta já está com este cargo."))
        }

        accountReq.role = request.role
        userRepository.save(accountReq)

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Cargo atualizado com sucesso. Login: ${request.login}, Cargo: ${request.role}"
            )
        )
    }

    @GetMapping("/roles")
    fun getRoles(): ResponseEntity<Any> {
        val allRoles = Role.entries.map { it.name }
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "roles" to allRoles
        ))
    }

    @PatchMapping("/accounts/plan")
    fun setRole(@RequestBody request: UserSetPlanSchema): ResponseEntity<Any> {
        val accountReq = userRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Conta não encontrada."))

        if (accountReq.plan == request.plan) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("success" to false, "message" to "A conta já está com este plano."))
        }

        accountReq.plan = request.plan
        userRepository.save(accountReq)

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Plano atualizado com sucesso. Login: ${request.login}, Plano: ${request.plan}"
            )
        )
    }

    @GetMapping("/plans")
    fun getPlans(): ResponseEntity<Any> {
        val allPlans = Plan.entries.map { it.name }
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "roles" to allPlans
        ))
    }

    @PatchMapping("/accounts/ban")
    fun banAccount(@RequestBody request: BanAccountSchema): ResponseEntity<Any> {
        val accountReq = userRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("success" to false, "message" to "Conta não encontrada."))


        if (request.duration == null || request.unit == null) {
            accountReq.banned = true
            accountReq.bannedAt = null
            accountReq.banExpiresAt = null

            accountReq.tokenVersion += 1
            userRepository.save(accountReq)

            val auth = SecurityContextHolder.getContext().authentication
            val principal = auth?.principal

            if (principal is CustomUserDetails && principal.user.id == accountReq.id) {
                SecurityContextHolder.clearContext()
            }

            return ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Conta banida com sucesso. Login: ${request.login}, Tempo: Permanente."
                )
            )
        }

        val now = LocalDateTime.now()
        val banExpiresAt = now.plus(request.duration, request.unit)

        accountReq.banned = true
        accountReq.bannedAt = now
        accountReq.banExpiresAt = banExpiresAt

        accountReq.tokenVersion += 1
        userRepository.save(accountReq)

        val auth = SecurityContextHolder.getContext().authentication
        val principal = auth?.principal

        if (principal is CustomUserDetails && principal.user.id == accountReq.id) {
            SecurityContextHolder.clearContext()
        }

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Conta banida com sucesso. Login: ${request.login}, Tempo: ${request.duration} ${request.unit}."
            )
        )
    }

    @PatchMapping("/accounts/unban/{login}")
    fun unbanAccount(@PathVariable login: String): ResponseEntity<Any> {
        val accountReq = userRepository.findByUsernameOrEmail(login, login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Conta não encontrada."))

        if (!accountReq.banned) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("success" to false, "message" to "A conta não está banida."))
        }

        accountReq.banned = false
        accountReq.bannedAt = null
        accountReq.banExpiresAt = null
        userRepository.save(accountReq)

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Conta desbanida com sucesso. Login: $login"
            )
        )
    }

    @GetMapping("/accounts/bans")
    fun getBans(): ResponseEntity<Any> {
        val accountsBanned = userRepository.findByBanned(true)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Nenhuma conta banida encontrada."))
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "accountsBanned" to accountsBanned
        ))
    }

    @DeleteMapping("/accounts/{login}")
    fun delAccount(@PathVariable login: String): ResponseEntity<Any> {
        val accountReq = userRepository.findByUsernameOrEmail(login, login)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("success" to false, "message" to "Conta não encontrada."))

        userRepository.delete(accountReq)

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Conta deletada com sucesso. Login: $login"
            )
        )
    }
}