package com.fixing.api.configs
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "api.auth")
class AuthConfig {
    var minUsernameLength: Int = 4
    var maxUsernameLength: Int = 15
    var minPasswordLength: Int = 6
    var maxPasswordLength: Int = 15
    var maxAttempts: Int = 5
    var lockoutMinutes: Long = 5
}