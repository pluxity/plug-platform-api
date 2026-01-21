package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.SceneExecution
import org.springframework.data.jpa.repository.JpaRepository

interface SceneExecutionRepository : JpaRepository<SceneExecution, Long>
