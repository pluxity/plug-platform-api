package com.pluxity.patrol.repository

import com.pluxity.patrol.entity.Trigger
import org.springframework.data.jpa.repository.JpaRepository

interface TriggerRepository : JpaRepository<Trigger, Long>
