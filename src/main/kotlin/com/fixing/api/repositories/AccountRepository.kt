package com.fixing.api.repositories

import com.fixing.api.enums.AccountStatus
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
    fun findByBanned(banned: Boolean): List<User>?
    fun findByAccountStatus(accountStatus: AccountStatus): List<User>?
}
