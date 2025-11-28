package com.fixing.api.repositories

import com.fixing.api.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AccountRepository : JpaRepository<User, Long> {
    fun findByUsernameOrEmail(username: String, email: String): User?
    fun findByCpf(cpf: String): User?
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByPhone(phone: String): User?
    fun findByBanned(banned: Boolean): User?

    @Query("SELECT u FROM User u WHERE u.cpf = :cpf OR u.username = :username OR u.email = :email OR u.phone = :phone")
    fun findExistingUser(
        @Param("cpf") cpf: String,
        @Param("username") username: String,
        @Param("email") email: String,
        @Param("phone") phone: String
    ): List<User>
}
