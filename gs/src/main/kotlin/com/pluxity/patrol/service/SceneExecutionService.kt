package com.pluxity.patrol.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.entity.SceneExecution
import com.pluxity.patrol.repository.ScenarioExecutionRepository
import com.pluxity.patrol.repository.SceneExecutionRepository
import com.pluxity.patrol.repository.SceneRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SceneExecutionService(
    private val repository: SceneExecutionRepository,
    private val scenarioExecutionRepository: ScenarioExecutionRepository,
    private val sceneRepository: SceneRepository,
) {
    fun start(
        scenarioExecutionId: Long,
        sceneId: Long,
    ): Long {
        val scenarioExecution =
            scenarioExecutionRepository.findByIdOrNull(scenarioExecutionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION, scenarioExecutionId)

        val scene =
            sceneRepository.findByIdOrNull(sceneId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, sceneId)

        val sceneExecution =
            repository.save(
                SceneExecution(
                    scenarioExecution = scenarioExecution,
                    scene = scene,
                ),
            )

        scenarioExecution.sceneExecutions.add(sceneExecution)

        return sceneExecution.requiredId
    }

    fun complete(sceneExecutionId: Long) {
        val sceneExecution =
            repository.findByIdOrNull(sceneExecutionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE_EXECUTION, sceneExecutionId)
        sceneExecution.complete()
    }

    fun fail(
        sceneExecutionId: Long,
        errorMessage: String?,
    ) {
        val sceneExecution =
            repository.findByIdOrNull(sceneExecutionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE_EXECUTION, sceneExecutionId)
        sceneExecution.fail(errorMessage)
    }

    fun skip(sceneExecutionId: Long) {
        val sceneExecution =
            repository.findByIdOrNull(sceneExecutionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE_EXECUTION, sceneExecutionId)
        sceneExecution.skip()
    }
}
