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

    fun parseNextExecutionTime(expression: String): LocalDateTime? = parseNextExecutionTime(expression, LocalDateTime.now())

    fun parseNextExecutionTimeWithStartDate(
        expression: String,
        startDate: LocalDateTime,
    ): LocalDateTime? = parseNextExecutionTime(expression, startDate)

    private fun parseNextExecutionTime(
        expression: String,
        baseTime: LocalDateTime,
    ): LocalDateTime? {
        val cron =
            runCatching { parser.parse(expression) }.getOrNull()
                ?: return null

        val executionTime = ExecutionTime.forCron(cron)
        return executionTime
            .nextExecution(ZonedDateTime.of(baseTime, ZoneId.systemDefault()))
            .map { it.toLocalDateTime().withSecond(0).withNano(0) }
            .orElse(null)
    }
}
