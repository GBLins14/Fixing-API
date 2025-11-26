package com.fixing.api.repositories

import com.fixing.api.models.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    /*fun findByCpf(cpf: String): User?*/
    fun findByUsernameOrEmail(username: String, email: String): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    /*fun findByPhone(phone: String): User?*/
    fun findByBanned(banned: Boolean): User?
}
