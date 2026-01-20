package com.pluxity.messaging

import com.pluxity.messaging.dto.ScenarioExecutionCancelMessage
import com.pluxity.messaging.dto.ScenarioExecutionCompleteMessage
import com.pluxity.messaging.dto.ScenarioExecutionStartMessage
import com.pluxity.patrol.service.ScenarioExecutionService
import io.github.springwolf.bindings.stomp.annotations.StompAsyncOperationBinding
import io.github.springwolf.core.asyncapi.annotations.AsyncListener
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.RestController

@RestController
class ScenarioExecutionMessageHandler(
    private val scenarioExecutionService: ScenarioExecutionService,
) {
    companion object {
        const val DESTINATION_START = "/app/scenario/execution/start"
        const val DESTINATION_COMPLETE = "/app/scenario/execution/complete"
        const val DESTINATION_CANCEL = "/app/scenario/execution/cancel"
    }

    @MessageMapping("/scenario/execution/start")
    @AsyncListener(
        operation =
            AsyncOperation(
                channelName = DESTINATION_START,
                description = "클라이언트가 시나리오 실행 시작을 알림",
                payloadType = ScenarioExecutionStartMessage::class,
            ),
    )
    @StompAsyncOperationBinding
    fun handleStart(
        @Payload message: ScenarioExecutionStartMessage,
    ) {
        scenarioExecutionService.start(message.scenarioExecutionId)
    }

    @MessageMapping("/scenario/execution/complete")
    @AsyncListener(
        operation =
            AsyncOperation(
                channelName = DESTINATION_COMPLETE,
                description = "클라이언트가 시나리오 실행 완료/실패를 알림",
                payloadType = ScenarioExecutionCompleteMessage::class,
            ),
    )
    @StompAsyncOperationBinding
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
    @AsyncListener(
        operation =
            AsyncOperation(
                channelName = DESTINATION_CANCEL,
                description = "클라이언트가 시나리오 실행 취소를 알림",
                payloadType = ScenarioExecutionCancelMessage::class,
            ),
    )
    @StompAsyncOperationBinding
    fun handleCancel(
        @Payload message: ScenarioExecutionCancelMessage,
    ) {
        scenarioExecutionService.cancel(message.scenarioExecutionId)
    }
}
