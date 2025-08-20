package com.pluxity.cctv.repository

import com.pluxity.cctv.entity.Cctv
import org.springframework.data.jpa.repository.JpaRepository

interface CctvRepository : JpaRepository<Cctv, String>
