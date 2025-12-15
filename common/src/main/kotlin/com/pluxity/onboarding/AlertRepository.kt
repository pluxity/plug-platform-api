package com.pluxity.onboarding

import org.springframework.data.jpa.repository.JpaRepository

interface AlertRepository : JpaRepository<Alert, Long>
