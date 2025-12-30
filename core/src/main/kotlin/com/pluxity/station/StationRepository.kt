package com.pluxity.station

import org.springframework.data.jpa.repository.JpaRepository

interface StationRepository : JpaRepository<Station, Long>
