package com.fixing.api.controllers

import com.fixing.api.configs.AuthConfig
import com.fixing.api.models.CustomUserDetails
import com.fixing.api.models.Role
import com.fixing.api.models.User
import com.fixing.api.repositories.UserRepository
import com.fixing.api.schemas.UserSignInSchema
import com.fixing.api.schemas.UserSignUpSchema
import com.fixing.api.security.Hash
import com.fixing.api.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import kotlin.Long
import kotlin.String

fun String.isValidCpf(): Boolean {
    val cleanedCpf = this.filter { it.isDigit() }
    if (cleanedCpf.length != 11) return false
    if (cleanedCpf.all { it == cleanedCpf[0] }) return false
    val numbers = cleanedCpf.map { it.toString().toInt() }
    val firstSum = (0 until 9).sumOf { (10 - it) * numbers[it] }
    val firstDigit = (firstSum * 10 % 11).let { if (it == 10) 0 else it }
    val secondSum = (0 until 10).sumOf { (11 - it) * numbers[it] }
    val secondDigit = (secondSum * 10 % 11).let { if (it == 10) 0 else it }
    return numbers[9] == firstDigit && numbers[10] == secondDigit
}

fun String.cleanCpf(): String = this.filter { it.isDigit() }

fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return this.matches(emailRegex)
}


fun String.isValidPhone(): Boolean {
    val phoneRegex = "^[0-9]{10,11}$".toRegex()
    return this.matches(phoneRegex)
}

fun checkDuplicate(value: Any?, message: String): ResponseEntity<Any>? {
    return if (value != null) ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("success" to false, "message" to message)) else null
}

@RestController
@RequestMapping("/auth")
class AccountController(private val authConfig: AuthConfig, private val userRepository: UserRepository, private val jwtUtil: JwtUtil, private val bcrypt: Hash) {
    
    @GetMapping("/me")
    fun me(): ResponseEntity<Any> {
        val auth = SecurityContextHolder.getContext().authentication
        val principal = auth?.principal

        if (principal !is CustomUserDetails) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Token inválido ou expirado."))
        }

        val user = principal.user

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "id" to user.id,
                "username" to user.username,
                "email" to user.email,
                "role" to user.role.name,
                "plan" to user.plan,
                "banned" to user.banned,
                "bannedAt" to user.bannedAt,
                "createdAt" to user.createdAt,
                "updatedAt" to user.updatedAt
            )
        )
    }

    @PostMapping("/sign-up")
    fun signUp(@RequestBody request: UserSignUpSchema): ResponseEntity<Any> {
        /*
                val cleanedCpf = request.cpf.cleanCpf()

                if (!cleanedCpf.isValidCpf()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(mapOf("success" to false, "message" to "É necessário inserir um número de CPF que seja válido."))
                }
        */

        if (request.username.length < authConfig.minUsernameLength || request.username.length > authConfig.maxUsernameLength) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "O nome de usuário deve conter no mínimo ${authConfig.minUsernameLength} caracteres, e no máximo ${authConfig.maxUsernameLength} caracteres."))
        }

        if (!request.email.isValidEmail()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "É necessário inserir um endereço de email que seja válido."))
        }

        /*        if (!request.phone.isValidPhone()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(mapOf("success" to false, "message" to "É necessário inserir um número de telefone que seja válido."))
                }*/

        if (request.password.length < authConfig.minPasswordLength || request.password.length > authConfig.maxPasswordLength) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "A senha deve conter no mínimo ${authConfig.minPasswordLength} caracteres, e no máximo ${authConfig.maxPasswordLength} caracteres."))
        }

        /*        val existingCpf = userRepository.findByCpf(cleanedCpf)*/
        val existingUsername = userRepository.findByUsername(request.username)
        val existingEmail = userRepository.findByEmail(request.email)
        /*        val existingPhone = userRepository.findByPhone(request.phone)*/

        /*        checkDuplicate(existingCpf, "Já existe uma conta registrada com este número de CPF.")?.let { return it }*/
        checkDuplicate(existingUsername, "Já existe uma conta registrada com este nome de usuário.")?.let { return it }
        checkDuplicate(existingEmail, "Já existe uma conta registrada com este endereço de email.")?.let { return it }
        /*        checkDuplicate(existingPhone, "Já existe uma conta registrada com este número de telefone.")?.let { return it }*/

        val user = User(
            /*            cpf = cleanedCpf,*/
            /*            name = request.name,*/
            username = request.username,
            email = request.email,
            /*            phone = request.phone,*/
            hashedPassword = bcrypt.encodePassword(request.password)
        )

        val account = userRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("success" to true, "message" to "Conta registrada com sucesso."))
    }

    @PostMapping("/sign-in")
    fun signIn(@RequestBody request: UserSignInSchema): ResponseEntity<Any> {
        val user = userRepository.findByUsernameOrEmail(request.login, request.login)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Usuário ou senha incorretos."))

        val now = LocalDateTime.now()

        if (user.banned && user.banExpiresAt?.isAfter(now) == true) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to "Conta temporariamente bloqueada."
                )
            )
        }

        if (user.banned && user.banExpiresAt?.isBefore(now) == true) {
            user.banned = false
            user.bannedAt = null
            user.banExpiresAt = null
            user.failedLoginAttempts = 0
            userRepository.save(user)
        }

        if (user.banned && user.banExpiresAt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                mapOf(
                    "success" to false,
                    "message" to "Conta permanentemente bloqueada."
                )
            )
        }

        if (!bcrypt.checkPassword(request.password, user.hashedPassword)) {
            user.failedLoginAttempts += 1

            if (user.failedLoginAttempts >= authConfig.maxAttempts) {
                user.banned = true
                user.bannedAt = now
                user.banExpiresAt = now.plusMinutes(authConfig.lockoutMinutes)
                userRepository.save(user)

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("success" to false, "message" to "Conta bloqueada devido a tentativas excessivas."))
            }

            userRepository.save(user)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Usuário ou senha incorretos."))
        }

        user.failedLoginAttempts = 0
        userRepository.save(user)

        val token = jwtUtil.generateToken(user.username, user.tokenVersion)

        return ResponseEntity.ok(mapOf("success" to true, "token" to token))
    }

    @GetMapping("/logout")
    fun logout(): ResponseEntity<Any> {
        val auth = SecurityContextHolder.getContext().authentication
        val principal = auth?.principal

        if (principal !is CustomUserDetails) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Token inválido ou expirado."))
        }

        val user = principal.user

        user.tokenVersion += 1
        userRepository.save(user)

        SecurityContextHolder.clearContext()

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "Logout realizado com sucesso."
            )
        )
    }

    @GetMapping("/token")
    fun checkToken(): ResponseEntity<Any> {
        val auth = SecurityContextHolder.getContext().authentication
        val principal = auth?.principal

        if (principal !is CustomUserDetails) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("success" to false, "message" to "Token inválido ou expirado."))
        }

        val user = principal.user

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "id" to user.id,
                "username" to user.username
            )
        )
    }
}