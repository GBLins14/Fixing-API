package com.fixing.api.schemas

import com.fixing.api.models.Role
import com.fixing.api.models.Plan
import java.time.temporal.ChronoUnit
//import com.fasterxml.jackson.annotation.JsonProperty

data class UserSignUpSchema(
   /* val cpf: String,
    @JsonProperty(required = false)
    val name: String? = null,*/
    val username: String,
    val email: String,
    /*val phone: String,*/
    val password: String
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