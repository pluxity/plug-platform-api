package com.pluxity.permission

import org.springframework.data.jpa.repository.JpaRepository

interface ResourcePermissionRepository : JpaRepository<ResourcePermission, Long>
