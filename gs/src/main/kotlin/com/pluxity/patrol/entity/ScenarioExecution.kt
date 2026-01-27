package com.pluxity.patrol.entity

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import java.time.LocalDateTime

@Entity
class ScenarioExecution(
    val scenarioName: String,
    val facilityName: String,
    @OneToMany(mappedBy = "scenarioExecution", cascade = [CascadeType.ALL], orphanRemoval = true)
    val sceneExecutions: MutableList<SceneExecution> = mutableListOf(),
    @Enumerated(EnumType.STRING)
    var triggerType: TriggerSource,
    @Enumerated(EnumType.STRING)
    var executionStatus: ScenarioExecutionStatus,
    val triggeredAt: LocalDateTime? = null,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    @Column(columnDefinition = "TEXT")
    var errorMessage: String? = null,
) : IdentityIdEntity() {
    fun addSceneExecution(sceneExecution: SceneExecution) {
        this.sceneExecutions.add(sceneExecution)
        sceneExecution.scenarioExecution = this
    }

    fun complete(at: LocalDateTime) {
        if (executionStatus != ScenarioExecutionStatus.RUNNING &&
            executionStatus != ScenarioExecutionStatus.TRIGGERED
        ) {
            throw CustomException(ErrorCode.INVALID_EXECUTION_STATUS, executionStatus, "완료")
        }
        finishedAt = at
        executionStatus = ScenarioExecutionStatus.COMPLETED
    }

    fun fail(
        at: LocalDateTime,
        errorMessage: String?,
    ) {
        if (executionStatus != ScenarioExecutionStatus.RUNNING &&
            executionStatus != ScenarioExecutionStatus.TRIGGERED
        ) {
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
