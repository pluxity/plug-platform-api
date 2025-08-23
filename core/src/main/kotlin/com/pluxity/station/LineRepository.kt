package com.pluxity.station

import org.springframework.data.jpa.repository.JpaRepository

interface LineRepository : JpaRepository<Line, Long> {
    fun findByName(name: String): Line?

    fun findByNameAndIdNot(
        name: String,
        id: Long,
    ): Line?
}
