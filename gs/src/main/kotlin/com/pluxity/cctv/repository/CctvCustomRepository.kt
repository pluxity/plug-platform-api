package com.pluxity.cctv.repository

import com.pluxity.cctv.entity.Cctv

interface CctvCustomRepository {
    fun findAllByFacilityIdIfPresent(facilityId: Long?): List<Cctv>
}
