package com.fixing.api.schemas

import com.fixing.api.enums.Plan
import com.fixing.api.enums.Role
import com.fixing.api.enums.TypeAccount
import java.time.temporal.ChronoUnit

data class UserSignUpSchema(
    val typeAccount: TypeAccount,
    val cpf: String,
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String,
    val password: String,
    val fotoSelfie: String? = null,
    val fotoRgFrente: String? = null,
    val fotoRgVerso: String? = null
)

data class UserSignInSchema(
    val login: String,
    val password: String
)

data class BanAccountSchema(
    val login: String,
    val duration: Long? = null,
    val unit: ChronoUnit? = null
)

data class UserSetRoleSchema(
    val login: String,
    val role: Role
)

data class UserSetPlanSchema(
    val login: String,
    val plan: Plan
)
