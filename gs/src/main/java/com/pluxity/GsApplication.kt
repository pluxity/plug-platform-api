package com.pluxity

import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class GsApplication

fun main(args: Array<String>) {
    runApplication<GsApplication>(*args)
}
