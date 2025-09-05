package com.pluxity

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = ["com.pluxity"])
object TestApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(TestApplication::class.java, *args)
    }
}
