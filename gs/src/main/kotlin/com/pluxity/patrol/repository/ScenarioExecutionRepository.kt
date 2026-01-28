package com.pluxity.patrol.repository

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.pluxity.patrol.entity.ScenarioExecution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ScenarioExecutionRepository :
    JpaRepository<ScenarioExecution, Long>,
    KotlinJdslJpqlExecutor {
    @Query(
        """
        SELECT se FROM ScenarioExecution se
        LEFT JOIN FETCH se.sceneExecutions sce
        WHERE se.id = :id
        """,
    )
    fun findByIdWithDetails(
        @Param("id") id: Long,
    ): ScenarioExecution?
}
