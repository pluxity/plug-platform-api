package com.pluxity.park

import org.springframework.data.jpa.repository.JpaRepository

interface ParkRepository : JpaRepository<Park, Long>
