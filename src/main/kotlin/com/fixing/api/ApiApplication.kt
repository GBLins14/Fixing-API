package com.fixing.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
//import org.springframework.cache.annotation.EnableCaching - Descomente caso queira liberar memoria da API!

@SpringBootApplication
//@EnableCaching - Descomente caso queira liberar memoria da API!
class RestfulApplication

fun main(args: Array<String>) {
    runApplication<RestfulApplication>(*args)
}