package com.pluxity.messaging

import com.pluxity.messaging.dto.ScenarioExecutionCancelMessage
import com.pluxity.messaging.dto.ScenarioExecutionCompleteMessage
import com.pluxity.messaging.dto.ScenarioExecutionStartMessage
import com.pluxity.patrol.service.ScenarioExecutionService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.RestController

@RestController
class ScenarioExecutionMessageHandler(
    private val scenarioExecutionService: ScenarioExecutionService,
) {
    @MessageMapping("/scenario/execution/start")
    fun handleStart(
        @Payload message: ScenarioExecutionStartMessage,
    ) {
        scenarioExecutionService.start(message.scenarioExecutionId)
    }

    @MessageMapping("/scenario/execution/complete")
    fun handleComplete(
        @Payload message: ScenarioExecutionCompleteMessage,
    ) {
        scenarioExecutionService.complete(
            message.scenarioExecutionId,
            message.success,
            message.errorMessage,
        )
    }

    @MessageMapping("/scenario/execution/cancel")
    fun handleCancel(
        @Payload message: ScenarioExecutionCancelMessage,
    ) {
        scenarioExecutionService.cancel(message.scenarioExecutionId)
    }
}
