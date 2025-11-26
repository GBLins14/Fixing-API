package com.fixing.api.security

import com.fixing.api.models.CustomUserDetails
import com.fixing.api.repositories.UserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.LocalDateTime

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val token = request.getHeader("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        if (token != null && jwtUtil.validateToken(token)) {

            val username = jwtUtil.getUsername(token)
            val user = userRepository.findByUsername(username)

            if (user != null) {

                val tokenVersion = jwtUtil.getTokenVersion(token)
                if (tokenVersion != user.tokenVersion) {
                    if (unauthorized(response, "Token inválido.")) return
                }

                if (user.banned) {
                    val now = LocalDateTime.now()
                    val banExpiresAt = user.banExpiresAt
                        ?: run {
                            if (unauthorized(response, "Conta permanentemente bloqueada.")) return
                            return
                        }

                    if (banExpiresAt.isAfter(now)) {
                        if (unauthorized(response, "Conta temporariamente bloqueada.")) return
                    }

                    if (banExpiresAt.isBefore(now)) {
                        user.banned = false
                        user.bannedAt = null
                        user.banExpiresAt = null
                        user.failedLoginAttempts = 0
                        userRepository.save(user)
                    }
                }

                val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

                val authentication = UsernamePasswordAuthenticationToken(
                    CustomUserDetails(user),
                    null,
                    authorities
                )

                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun unauthorized(response: HttpServletResponse, msg: String): Boolean {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = "text/plain"
        response.writer.write(msg)
        return true
    }
}
