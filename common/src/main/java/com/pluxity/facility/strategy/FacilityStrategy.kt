package com.pluxity.facility.strategy

import com.pluxity.facility.Facility
import org.springframework.stereotype.Service

@Service
interface FacilityStrategy<REQ, RES> {
    fun <T : Facility?> save(facility: T?, data: REQ?)

    fun <T : Facility?> findByFacility(facility: T?): RES?

    fun <T : Facility?> findAllByFacility(facility: T?): MutableList<RES?>?

    fun <T : Facility?> update(facility: T?, data: REQ?)

    fun <T : Facility?> delete(facility: T?)
}
