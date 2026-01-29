package com.pluxity.patrol.scheduler

import com.pluxity.messaging.dto.ScenarioTriggerBatchEvent
import com.pluxity.patrol.constant.CronDayOfWeek
import com.pluxity.patrol.repository.TriggerRepository
import com.pluxity.patrol.service.ScenarioExecutionService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class TriggerScheduler(
    private val triggerRepository: TriggerRepository,
    private val scenarioExecutionService: ScenarioExecutionService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "0 0/1 * * * *")
    fun checkAndExecuteTriggers() {
        val now = LocalDateTime.now().withSecond(0).withNano(0)
        val triggers =
            triggerRepository.findActiveTriggers(
                month = now.monthValue,
                dayOfMonth = now.dayOfMonth,
                hour = now.hour,
                minute = now.minute,
                dayOfWeekBit = CronDayOfWeek.fromDayOfWeek(now.dayOfWeek).bit,
                currentDate = now.toLocalDate(),
            )
        val results =
            triggers.map { trigger ->
                scenarioExecutionService.execute(trigger.scenario, trigger)
            }
        if (results.isNotEmpty()) {
            eventPublisher.publishEvent(ScenarioTriggerBatchEvent(results))
            log.info { "실행된 트리거: ${results.size}" }
        }
    }
}
