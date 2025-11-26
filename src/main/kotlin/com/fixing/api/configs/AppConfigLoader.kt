package com.fixing.api.configs

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource(
    value = ["classpath:config/config.yml"],
    factory = YamlPropertyLoaderFactory::class
)
class AppConfigLoader
