package com.pluxity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class GsApplication

fun main(args: Array<String>) {
    runApplication<GsApplication>(*args)
}
