package com.pluxity.patrol.utils

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object CronParserUtils {
    private val cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
    private val parser = CronParser(cronDefinition)

    fun parseNextExecutionTime(expression: String): LocalDateTime {
        val cron = parser.parse(expression)

        val executionTime = ExecutionTime.forCron(cron)

        return executionTime
            .nextExecution(ZonedDateTime.now(ZoneId.systemDefault()))
            .map { it.toLocalDateTime().withSecond(0).withNano(0) }
            .orElse(null)
    }
}
