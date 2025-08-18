package com.pluxity.building

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BuildingRepository : JpaRepository<Building, Long>
