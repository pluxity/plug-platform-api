package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.ScenarioExecution
import org.springframework.data.jpa.repository.JpaRepository

interface ScenarioExecutionRepository : JpaRepository<ScenarioExecution, Long>
