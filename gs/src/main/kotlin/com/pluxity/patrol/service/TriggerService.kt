package com.pluxity.patrol.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.Trigger
import com.pluxity.patrol.entity.TriggerTarget
import com.pluxity.patrol.repository.TriggerRepository
import com.pluxity.patrol.utils.CronParserUtils
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class TriggerService(
    private val triggerRepository: TriggerRepository,
) {
    fun createTrigger(
        request: TriggerRequest,
        scenario: Scenario,
    ): Trigger {
        val trigger =
            triggerRepository.save(
                Trigger(
                    scenario = scenario,
                    triggerType = request.triggerType,
                    startDate = request.startDate ?: LocalDate.now(),
                    endDate = request.endDate,
                    cronExpression = request.cronExpression,
                    isActive = request.isActive,
                    nextExecutionTime = calculateNextExecutionTime(request.cronExpression),
                ),
            )

        request.triggerTargetRequests
            ?.distinctBy { it.targetType to it.targetId }
            ?.forEach { target ->
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
        existingTrigger.updateTrigger(
            triggerType = request.triggerType,
            startDate = request.startDate ?: LocalDate.now(),
            endDate = request.endDate,
            cronExpression = request.cronExpression,
            isActive = request.isActive,
            nextExecutionTime = calculateNextExecutionTime(request.cronExpression),
        )

        existingTrigger.triggerTargets.clear()
        request.triggerTargetRequests
            ?.distinctBy { it.targetType to it.targetId }
            ?.forEach { target ->
                existingTrigger.addTriggerTarget(
                    TriggerTarget(
                        trigger = existingTrigger,
                        targetType = target.targetType,
                        targetId = target.targetId,
                    ),
                )
            }
    }

    fun updateNextExecutionTime(trigger: Trigger) {
        trigger.nextExecutionTime = calculateNextExecutionTime(trigger.cronExpression)
    }

    private fun calculateNextExecutionTime(cronExpression: String): LocalDateTime =
        try {
            CronParserUtils.parseNextExecutionTime(cronExpression)
        } catch (_: Exception) {
            throw CustomException(ErrorCode.INVALID_CRON_EXPRESSION, cronExpression)
        }
}
