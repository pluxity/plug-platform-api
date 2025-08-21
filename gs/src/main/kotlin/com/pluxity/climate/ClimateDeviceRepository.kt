package com.pluxity.climate

import org.springframework.data.jpa.repository.JpaRepository

interface ClimateDeviceRepository : JpaRepository<ClimateDevice, String>
