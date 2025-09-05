package com.pluxity.facility.strategy

import com.pluxity.facility.Facility
import com.pluxity.facility.floor.Floor
import com.pluxity.facility.floor.FloorRepository
import com.pluxity.facility.floor.dto.FloorRequest
import com.pluxity.facility.floor.dto.FloorResponse
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils
import java.util.function.Function
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
class FloorService {
    private val repository: FloorRepository? = null

    @Transactional
    fun <T : Facility?> save(facility: T?, floorRequests: MutableList<FloorRequest?>?) {
        if (CollectionUtils.isEmpty(floorRequests)) {
            return
        }

        val floors =
            floorRequests!!.stream().map<Floor?> { request: FloorRequest? -> toEntity(facility, request!!) }.toList()

        repository!!.saveAll<Floor?>(floors)
    }

    @Transactional
    fun <T : Facility?> update(facility: T?, floorRequests: MutableList<FloorRequest?>?) {
        repository!!.deleteByFacility(facility)

        save<T?>(facility, floorRequests)
    }

    @Transactional(readOnly = true)
    fun <T : Facility?> findAllByFacility(facility: T?): MutableList<FloorResponse?> {
        return repository!!.findAllByFacility(facility).stream().map<FloorResponse?> { floor: Floor? -> FloorResponse.Companion.from(floor) }.toList()
    }

    @Transactional(readOnly = true)
    fun <T : Facility?> findAllByFacilities(
        facilities: MutableList<T?>?
    ): MutableMap<Facility?, MutableList<FloorResponse?>?> {
        if (CollectionUtils.isEmpty(facilities)) {
            return mutableMapOf<Facility?, MutableList<FloorResponse?>?>()
        }
        return repository!!.findAllByFacilities<T?>(facilities).stream()
            .collect(
                Collectors.groupingBy(
                    Function { obj: Floor? -> obj!!.getFacility() }, Collectors.mapping(Function { floor: Floor? -> FloorResponse.Companion.from(floor) }, Collectors.toList())
                )
            )
    }

    @Transactional
    fun <T : Facility?> delete(facility: T?) {
        repository!!.deleteByFacility(facility)
    }

    private fun toEntity(facility: Facility?, request: FloorRequest): Floor? {
        return Floor.builder()
            .facility(facility)
            .floorId(request.floorId)
            .name(request.name)
            .build()
    }
}
