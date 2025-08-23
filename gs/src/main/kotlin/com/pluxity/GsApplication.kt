package com.pluxity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class GsApplication

fun main(args: Array<String>) {
    runApplication<GsApplication>(*args)
}
