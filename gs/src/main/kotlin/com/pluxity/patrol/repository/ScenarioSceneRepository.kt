package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.ScenarioScene
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ScenarioSceneRepository : JpaRepository<ScenarioScene, Long> {
    @Modifying
    @Query("DELETE FROM ScenarioScene ss WHERE ss.scene.id = :sceneId")
    fun deleteAllBySceneId(sceneId: Long): Int
}
