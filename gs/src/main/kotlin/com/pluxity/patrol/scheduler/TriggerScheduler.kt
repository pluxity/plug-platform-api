package com.pluxity.patrol.scheduler

import com.pluxity.patrol.constant.CronDayOfWeek
import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.entity.Trigger
import com.pluxity.patrol.repository.TriggerRepository
import com.pluxity.patrol.service.ScenarioExecutionService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class TriggerScheduler(
    private val triggerRepository: TriggerRepository,
    private val scenarioExecutionService: ScenarioExecutionService,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(fixedRate = 60000)
    @Transactional
    fun checkAndExecuteTriggers() {
        val now = LocalDateTime.now().withSecond(0).withNano(0)
        runCatching {
            processTriggersAtTime(now)
        }.onFailure { e ->
            log.error { "트리거 처리 중 오류 발생 $e" }
        }
    }

    fun processTriggersAtTime(now: LocalDateTime) {
        val triggers =
            triggerRepository.findActiveTriggers(
                month = now.monthValue,
                dayOfMonth = now.dayOfMonth,
                hour = now.hour,
                minute = now.minute,
                dayOfWeekBit = CronDayOfWeek.fromDayOfWeek(now.dayOfWeek).bit,
                currentDate = now.toLocalDate(),
            )

        triggers.forEach { execute(it) }

        if (triggers.isNotEmpty()) {
            log.info { "실행된 트리거: ${triggers.size}" }
        }
    }

    private fun execute(trigger: Trigger) {
        try {
            scenarioExecutionService.execute(trigger.scenario)

            if (trigger.triggerType == TriggerType.ONCE) {
                trigger.isActive = false
            }

            log.info {
                "트리거 실행 완료: triggerId=${trigger.id}, scenarioId=${trigger.scenario.id}"
            }
        } catch (e: Exception) {
            log.error { "트리거 실행 실패: triggerId=${trigger.id}, scenarioId=${trigger.scenario.id} $e" }
        }
    }
}
