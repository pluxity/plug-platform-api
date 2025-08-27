package com.pluxity.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@AutoConfiguration
@EnableScheduling
@ComponentScan("com.pluxity")
class CollectAutoConfiguration
