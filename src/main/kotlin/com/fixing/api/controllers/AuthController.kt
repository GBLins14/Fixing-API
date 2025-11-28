package com.fixing.api.controllers

import com.fixing.api.configs.AuthConfig
import com.fixing.api.models.CustomUserDetails
import com.fixing.api.models.User
import com.fixing.api.repositories.UserRepository
import com.fixing.api.schemas.UserSignInSchema
import com.fixing.api.schemas.UserSignUpSchema
import com.fixing.api.security.Hash
import com.fixing.api.security.JwtUtil
import com.fixing.api.utils.ValidatorUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

fun checkDuplicate(value: Any?, message: String): ResponseEntity<Any>? {
    return if (value != null) ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("success" to false, "message" to message)) else null
}

@RestController
@RequestMapping("/auth")
class AuthController(private val authConfig: AuthConfig, private val userRepository: UserRepository, private val jwtUtil: JwtUtil, private val bcrypt: Hash, private val validatorUtil: ValidatorUtil) {

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
/*        val cleanedCpf = validatorUtil.cleanCpf(request.cpf)

        if (!validatorUtil.isValidCpf(cleanedCpf)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "É necessário inserir um número de CPF que seja válido."))
        }*/

        if (request.username.length < authConfig.minUsernameLength || request.username.length > authConfig.maxUsernameLength) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "O nome de usuário deve conter no mínimo ${authConfig.minUsernameLength} caracteres, e no máximo ${authConfig.maxUsernameLength} caracteres."))
        }

        if (!validatorUtil.isValidEmail(request.email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "É necessário inserir um endereço de email que seja válido."))
        }

/*        if (!validatorUtil.isValidPhone(request.phone)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "É necessário inserir um número de telefone que seja válido."))
        }*/

        if (request.password.length < authConfig.minPasswordLength || request.password.length > authConfig.maxPasswordLength) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("success" to false, "message" to "A senha deve conter no mínimo ${authConfig.minPasswordLength} caracteres, e no máximo ${authConfig.maxPasswordLength} caracteres."))
        }

//        val existingCpf = userRepository.findByCpf(cleanedCpf)
        val existingUsername = userRepository.findByUsername(request.username)
        val existingEmail = userRepository.findByEmail(request.email)
//        val existingPhone = userRepository.findByPhone(request.phone)

//        checkDuplicate(existingCpf, "Já existe uma conta registrada com este número de CPF.")?.let { return it }
        checkDuplicate(existingUsername, "Já existe uma conta registrada com este nome de usuário.")?.let { return it }
        checkDuplicate(existingEmail, "Já existe uma conta registrada com este endereço de email.")?.let { return it }
//        checkDuplicate(existingPhone, "Já existe uma conta registrada com este número de telefone.")?.let { return it }

        val user = User(
//            cpf = cleanedCpf,
//            name = request.name,
            username = request.username,
            email = request.email,
//            phone = request.phone,
            hashedPassword = bcrypt.encodePassword(request.password)
        )

        userRepository.save(user)
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
