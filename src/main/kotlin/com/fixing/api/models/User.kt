package com.fixing.api.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fixing.api.enums.AccountStatus
import com.fixing.api.enums.TypeAccount
import com.fixing.api.enums.Plan
import com.fixing.api.enums.Role
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "type_account", nullable = false)
    @Enumerated(EnumType.STRING)
    var typeAccount: TypeAccount = TypeAccount.CLIENT,

    @Column(name = "profile_image_url", length = 500)
    var profileImageUrl: String? = null,

    @Column(nullable = false, unique = true)
    val cpf: String = "",

    @Column(nullable = false)
    var fullName: String? = null,

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false, unique = true)
    var phone: String = "",

    @JsonIgnore
    @Column(name = "hash_password", nullable = false)
    var hashedPassword: String = "",

    @Column(name = "rating", nullable = false)
    var rating: Double = 0.0,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var plan: Plan = Plan.FREE,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,

    @Column(name = "fotoSelfie")
    var fotoSelfie: String? = null,

    @Column(name = "fotoRg_F")
    var fotoRgFrente: String? = null,

    @Column(name = "fotoRg_V")
    var fotoRgVerso: String? = null,

    @Column(name = "accountStatus", nullable = false)
    @Enumerated(EnumType.STRING)
    var accountStatus: AccountStatus,

    @Column(nullable = false)
    var banned: Boolean = false,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var bannedAt: LocalDateTime? = null,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var banExpiresAt: LocalDateTime? = null,

    @Column(nullable = false)
    var failedLoginAttempts: Int = 0,

    @Column(nullable = false)
    var tokenVersion: Int = 0,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
