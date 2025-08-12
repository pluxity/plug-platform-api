package com.pluxity

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class GsApplication

fun main(args: Array<String>) {
    SpringApplication.run(GsApplication::class.java, *args)
}
