package com.pluxity.patrol.entity

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class ScenarioExecution(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    val scenario: Scenario,
    var triggerType: TriggerSource,
    var executionStatus: ScenarioExecutionStatus,
    val triggeredAt: LocalDateTime? = null,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var errorMessage: String? = null,
) : IdentityIdEntity() {
    fun complete(at: LocalDateTime) {
        if (executionStatus != ScenarioExecutionStatus.RUNNING) {
            throw CustomException(ErrorCode.INVALID_EXECUTION_STATUS, executionStatus, "완료")
        }
        finishedAt = at
        executionStatus = ScenarioExecutionStatus.COMPLETED
    }

    fun fail(
        at: LocalDateTime,
        errorMessage: String?,
    ) {
        if (executionStatus != ScenarioExecutionStatus.RUNNING) {
            throw CustomException(ErrorCode.INVALID_EXECUTION_STATUS, executionStatus, "실패 처리")
        }
        finishedAt = at
        executionStatus = ScenarioExecutionStatus.FAILED
        this.errorMessage = errorMessage
    }

    fun cancel(at: LocalDateTime) {
        if (executionStatus == ScenarioExecutionStatus.COMPLETED ||
            executionStatus == ScenarioExecutionStatus.FAILED ||
            executionStatus == ScenarioExecutionStatus.CANCELLED
        ) {
            throw CustomException(ErrorCode.INVALID_EXECUTION_STATUS, executionStatus, "취소")
        }
        finishedAt = at
        executionStatus = ScenarioExecutionStatus.CANCELLED
    }
}
