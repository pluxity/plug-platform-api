package com.pluxity.patrol.utils

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.field.CronFieldName
import com.cronutils.model.field.expression.Always
import com.cronutils.model.field.expression.And
import com.cronutils.model.field.expression.Between
import com.cronutils.model.field.expression.On
import com.cronutils.model.field.value.IntegerFieldValue
import com.cronutils.parser.CronParser
import com.pluxity.patrol.constant.CronDayOfWeek

object CronParserUtils {
    private val cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)
    private val parser = CronParser(cronDefinition)

    data class ParsedCron(
        val minute: Int?,
        val hour: Int?,
        val dayOfMonth: Int?,
        val month: Int?,
        val dayOfWeek: Int?,
    )

    fun parse(expression: String): ParsedCron {
        val cron = parser.parse(expression)

        fun getValue(field: CronFieldName): Int? {
            val expr = cron.retrieve(field).expression
            return if (expr is On) expr.time.value else null
        }

        fun getDayOfWeekBit(): Int {
            val field = cron.retrieve(CronFieldName.DAY_OF_WEEK)
            return when (val expr = field.expression) {
                is Always -> 127
                is On -> CronDayOfWeek.fromCronValue(expr.time.value).bit
                is And -> {
                    expr.expressions
                        .filterIsInstance<On>()
                        .sumOf { CronDayOfWeek.fromCronValue(it.time.value).bit }
                }
                is Between -> {
                    val start = (expr.from as IntegerFieldValue).value
                    val end = (expr.to as IntegerFieldValue).value

                    (start..end).sumOf { CronDayOfWeek.fromCronValue(it).bit }
                }
                else -> 0
            }
        }

        return ParsedCron(
            minute = getValue(CronFieldName.MINUTE),
            hour = getValue(CronFieldName.HOUR),
            dayOfMonth = getValue(CronFieldName.DAY_OF_MONTH),
            month = getValue(CronFieldName.MONTH),
            dayOfWeek = getDayOfWeekBit(),
        )
    }
}
