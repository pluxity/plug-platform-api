package com.pluxity.cctv

import base.entity.withId
import com.pluxity.permission.DomainPermission
import com.pluxity.permission.Permission
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourcePermission
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RolePermission
import com.pluxity.user.entity.User
import com.pluxity.user.service.UserService
import io.mockk.every
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

internal fun makeUserWithPermissions(
    resourcePermissions: List<ResourcePermission>,
    domainPermissions: List<DomainPermission> = emptyList(),
): User {
    val role = Role(name = "ROLE_USER", description = "role").withId(1L)
    if (resourcePermissions.isNotEmpty() || domainPermissions.isNotEmpty()) {
        val permission = Permission("CCTV 권한 그룹", null)
        resourcePermissions.forEach { permission.addResourcePermission(it) }
        domainPermissions.forEach { permission.addDomainPermission(it) }
        role.addRolePermission(RolePermission(role = role, permission = permission))
    }
    return User("tester", "pw", "name", null).withId(10L).apply { addRole(role) }
}

internal fun setUserWithPermission(
    userService: UserService,
    level: PermissionLevel,
    resourceId: String = "c1",
    includeDomain: Boolean = false,
    includeResource: Boolean = true,
) {
    val resourcePermissions =
        if (includeResource) {
            listOf(
                ResourcePermission(
                    resourceName = ResourceType.CCTV.name,
                    resourceId = resourceId,
                    level = level,
                ),
            )
        } else {
            emptyList()
        }
    val domainPermissions =
        if (includeDomain) {
            listOf(
                DomainPermission(
                    resourceName = ResourceType.CCTV.name,
                    level = level,
                ),
            )
        } else {
            emptyList()
        }
    val user =
        makeUserWithPermissions(
            resourcePermissions = resourcePermissions,
            domainPermissions = domainPermissions,
        )
    every { userService.findUserByUsername("tester") } returns user
}

internal fun setUserWithPermissions(
    userService: UserService,
    resourcePermissions: List<ResourcePermission>,
    domainPermissions: List<DomainPermission> = emptyList(),
) {
    val user = makeUserWithPermissions(resourcePermissions, domainPermissions)
    every { userService.findUserByUsername("tester") } returns user
}

internal fun initAuthUser(userService: UserService) {
    val user = makeUserWithPermissions(emptyList(), emptyList())
    SecurityContextHolder.getContext().authentication =
        UsernamePasswordAuthenticationToken("tester", null, emptyList())
    every { userService.findUserByUsername("tester") } returns user
}
