package com.pluxity.facility.strategy

import com.pluxity.facility.Facility
import com.pluxity.facility.floor.Floor
import com.pluxity.facility.floor.FloorRepository
import com.pluxity.facility.floor.dto.FloorRequest
import com.pluxity.facility.floor.dto.FloorResponse
import com.pluxity.facility.floor.dto.toFloorResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils

@Service
class FloorService(
    private val repository: FloorRepository,
) {
    @Transactional
    fun <T : Facility> save(
        facility: T,
        floorRequests: List<FloorRequest>?,
    ) {
        if (!floorRequests.isNullOrEmpty()) {
            val floors = floorRequests.map { request -> toEntity(facility, request) }
            repository.saveAll(floors)
        }
    }

    @Transactional
    fun <T : Facility> update(
        facility: T,
        floorRequests: List<FloorRequest>?,
    ) {
        repository.deleteByFacility(facility)
        save(facility, floorRequests)
    }

    @Transactional(readOnly = true)
    fun <T : Facility> findAllByFacility(facility: T): List<FloorResponse> =
        repository.findAllByFacility(facility).map { it.toFloorResponse() }

    @Transactional(readOnly = true)
    fun <T : Facility> findAllByFacilities(facilities: List<T>): Map<Facility, List<FloorResponse>> {
        if (CollectionUtils.isEmpty(facilities)) {
            return emptyMap()
        }
        return repository
            .findAllByFacilities(facilities)
            .groupBy({ it.facility!! }, { it.toFloorResponse() })
    }

    @Transactional
    fun <T : Facility> delete(facility: T) {
        repository.deleteByFacility(facility)
    }

    private fun toEntity(
        facility: Facility,
        request: FloorRequest,
    ): Floor =
        Floor(
            facility = facility,
            floorId = request.floorId,
            name = request.name,
        )
}
