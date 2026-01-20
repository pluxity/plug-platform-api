package com.pluxity.patrol.event

import com.pluxity.messaging.dto.ScenarioTriggerMessage
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ScenarioExecutionEventListener(
    private val messagingTemplate: SimpMessagingTemplate,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TOPIC_SCENARIO_EXECUTION = "/topic/scenario-execution"
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: ScenarioExecutedEvent) {
        log.info("시나리오 실행 이벤트 처리: {}", event)
        messagingTemplate.convertAndSend(
            TOPIC_SCENARIO_EXECUTION,
            ScenarioTriggerMessage(event.scenarioExecutionId, event.scenarioId),
        )
    }
}
