package com.pluxity.patrol.service

import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.Trigger
import com.pluxity.patrol.entity.TriggerTarget
import com.pluxity.patrol.repository.TriggerRepository
import com.pluxity.patrol.utils.CronParserUtils
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class TriggerService(
    val triggerRepository: TriggerRepository,
) {
    fun createTrigger(
        request: TriggerRequest,
        scenario: Scenario,
    ): Trigger {
        val parsedCron = CronParserUtils.parse(request.cronExpression)

        val trigger =
            triggerRepository.save(
                Trigger(
                    scenario = scenario,
                    triggerType = request.triggerType,
                    startDate = request.startDate ?: LocalDate.now(),
                    endDate = request.endDate,
                    cronExpression = request.cronExpression,
                    isActive = request.isActive,
                    executeHour = parsedCron.hour,
                    executeMinute = parsedCron.minute,
                    dayOfWeek = parsedCron.dayOfWeek ?: 0,
                    month = parsedCron.month,
                    dayOfMonth = parsedCron.dayOfMonth,
                ),
            )

        request.triggerTargetRequests?.forEach { target ->
            trigger.addTriggerTarget(
                TriggerTarget(
                    trigger = trigger,
                    targetType = target.targetType,
                    targetId = target.targetId,
                ),
            )
        }
        return trigger
    }

    fun updateTrigger(
        existingTrigger: Trigger,
        request: TriggerRequest,
    ) {
        val parsedCron = CronParserUtils.parse(request.cronExpression)

        existingTrigger.updateTrigger(
            triggerType = request.triggerType,
            startDate = request.startDate ?: LocalDate.now(),
            endDate = request.endDate,
            cronExpression = request.cronExpression,
            isActive = request.isActive,
            hour = parsedCron.hour!!,
            minute = parsedCron.minute!!,
            dayOfWeek = parsedCron.dayOfWeek!!,
            dayOfMonth = parsedCron.dayOfMonth,
            month = parsedCron.month,
        )
    }
}
