package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.Scene
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SceneRepository : JpaRepository<Scene, Long> {
    @Query(
        """
        SELECT s FROM Scene s
        LEFT JOIN FETCH s.sceneDeviceActions
        JOIN FETCH s.facility f
        WHERE s.id = :id
        AND  s.facility.id = :facilityId
    """,
    )
    fun findByIdAndFacilityIdWithDetails(
        id: Long,
        facilityId: Long,
    ): Scene?

    @Query(
        """
        SELECT s FROM Scene s
        LEFT JOIN FETCH s.sceneDeviceActions
        JOIN FETCH s.facility f
        WHERE  s.facility.id = :facilityId
    """,
    )
    fun findAllByFacilityIdWithDetails(facilityId: Long): List<Scene>

    fun deleteByIdAndFacilityId(
        id: Long,
        facilityId: Long,
    ): Long

    @Query(
        """
        SELECT s FROM Scene s join fetch s.facility WHERE s.id IN :ids AND s.facility.id = :facilityId
    """,
    )
    fun findAllByIdAndFacilityId(
        ids: List<Long>,
        facilityId: Long,
    ): List<Scene>
}
