package com.pluxity.patrol.entity

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.SceneExecutionStatus
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class SceneExecution(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_execution_id")
    val scenarioExecution: ScenarioExecution,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id")
    val scene: Scene,
    var status: SceneExecutionStatus = SceneExecutionStatus.RUNNING,
    val startedAt: LocalDateTime = LocalDateTime.now(),
    var finishedAt: LocalDateTime? = null,
    var errorMessage: String? = null,
) : IdentityIdEntity() {
    fun complete() {
        if (status != SceneExecutionStatus.RUNNING) {
            throw CustomException(ErrorCode.INVALID_SCENE_EXECUTION_STATUS, status, "완료")
        }
        finishedAt = LocalDateTime.now()
        status = SceneExecutionStatus.COMPLETED
    }

    fun fail(errorMessage: String?) {
        if (status != SceneExecutionStatus.RUNNING) {
            throw CustomException(ErrorCode.INVALID_SCENE_EXECUTION_STATUS, status, "실패 처리")
        }
        finishedAt = LocalDateTime.now()
        status = SceneExecutionStatus.FAILED
        this.errorMessage = errorMessage
    }

    fun skip() {
        if (status != SceneExecutionStatus.RUNNING && status != SceneExecutionStatus.PENDING) {
            throw CustomException(ErrorCode.INVALID_SCENE_EXECUTION_STATUS, status, "건너뛰기")
        }
        finishedAt = LocalDateTime.now()
        status = SceneExecutionStatus.SKIPPED
    }
}
