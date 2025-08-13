package com.pluxity.device

import org.springframework.data.jpa.repository.JpaRepository

interface GsDeviceRepository : JpaRepository<GsDevice, String>
