package com.pluxity.permission

import org.springframework.data.jpa.repository.JpaRepository

interface DomainPermissionRepository : JpaRepository<DomainPermission, Long>
