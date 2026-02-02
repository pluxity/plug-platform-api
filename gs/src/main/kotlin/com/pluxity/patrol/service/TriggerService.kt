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
import java.time.LocalTime

@Service
class TriggerService(
    private val triggerRepository: TriggerRepository,
) {
    /**
     * 트리거 생성 (사용자 요청)
     * - endDate가 다음 실행시간보다 이전이면 예외 발생
     */
    fun createTrigger(
        request: TriggerRequest,
        scenario: Scenario,
    ): Trigger {
        validateDateRange(request.startDate, request.endDate)

        val nextExecutionTime = calculateNextExecutionTime(request.cronExpression, request.startDate)
        validateEndDate(request.endDate, nextExecutionTime)

        val trigger =
            triggerRepository.save(
                Trigger(
                    scenario = scenario,
                    triggerType = request.triggerType,
                    startDate = request.startDate ?: LocalDate.now(),
                    endDate = request.endDate,
                    cronExpression = request.cronExpression,
                    isActive = request.isActive,
                    nextExecutionTime = nextExecutionTime,
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

    /**
     * 트리거 수정 (사용자 요청)
     * - endDate가 다음 실행시간보다 이전이면 예외 발생
     */
    fun updateTrigger(
        existingTrigger: Trigger,
        request: TriggerRequest,
    ) {
        validateDateRange(request.startDate, request.endDate)

        val nextExecutionTime = calculateNextExecutionTime(request.cronExpression, request.startDate)
        validateEndDate(request.endDate, nextExecutionTime)

        existingTrigger.updateTrigger(
            triggerType = request.triggerType,
            startDate = request.startDate ?: LocalDate.now(),
            endDate = request.endDate,
            cronExpression = request.cronExpression,
            isActive = request.isActive,
            nextExecutionTime = nextExecutionTime,
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

    /**
     * 다음 실행 시간 갱신 (스케줄러 호출)
     * - 시나리오 실행 후 호출되어 다음 실행 시간 계산
     * - 유효기간 만료 시 예외 대신 비활성화 처리
     */
    fun updateNextExecutionTime(trigger: Trigger) {
        val nextExecutionTime = calculateNextExecutionTime(trigger.cronExpression, trigger.startDate)
        val endDate = trigger.endDate

        val isExpired =
            endDate != null && endDate.isBefore(nextExecutionTime.toLocalDate())

        if (isExpired) {
            trigger.isActive = false
            trigger.nextExecutionTime = null
            return
        }

        trigger.nextExecutionTime = nextExecutionTime
    }

    private fun calculateNextExecutionTime(
        expression: String,
        startDate: LocalDate?,
    ): LocalDateTime {
        val nextExecution =
            CronParserUtils.parseNextExecutionTime(expression)
                ?: throw CustomException(ErrorCode.INVALID_CRON_EXPRESSION, expression)

        return if (startDate != null && nextExecution.toLocalDate().isBefore(startDate)) {
            CronParserUtils.parseNextExecutionTimeWithStartDate(
                expression,
                LocalDateTime.of(startDate, LocalTime.MIDNIGHT),
            ) ?: throw CustomException(ErrorCode.INVALID_CRON_EXPRESSION, expression)
        } else {
            nextExecution
        }
    }

    private fun validateEndDate(
        endDate: LocalDate?,
        nextExecutionTime: LocalDateTime,
    ) {
        if (endDate != null && endDate.isBefore(nextExecutionTime.toLocalDate())) {
            throw CustomException(ErrorCode.END_DATE_ALREADY_PASSED, endDate, nextExecutionTime)
        }
    }

    private fun validateDateRange(
        startDate: LocalDate?,
        endDate: LocalDate?,
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw CustomException(ErrorCode.START_DATE_AFTER_END_DATE, startDate, endDate)
        }
    }
}
