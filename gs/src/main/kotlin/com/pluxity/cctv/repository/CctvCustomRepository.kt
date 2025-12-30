package com.pluxity.cctv.repository

import com.pluxity.cctv.entity.Cctv

interface CctvCustomRepository {
    fun findByIdOrNullCustom(id: String): Cctv?

    fun findAllByFacilityIdIfPresent(facilityId: Long?): List<Cctv>
}
