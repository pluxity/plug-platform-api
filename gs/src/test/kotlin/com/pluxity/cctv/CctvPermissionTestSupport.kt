package com.pluxity.cctv

import base.entity.withId
import com.pluxity.permission.Permission
import com.pluxity.permission.PermissionGroup
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RolePermission
import com.pluxity.user.entity.User
import com.pluxity.user.repository.RoleGlobalPolicyRepository
import com.pluxity.user.service.UserService
import io.mockk.every
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

internal fun makeUserWithPermissions(permissions: List<Permission>): User {
    val role = Role(name = "ROLE_USER", description = "role").withId(1L)
    if (permissions.isNotEmpty()) {
        val permissionGroup = PermissionGroup("CCTV 권한 그룹", null)
        permissions.forEach { permissionGroup.addPermission(it) }
        role.addRolePermission(RolePermission(role = role, permissionGroup = permissionGroup))
    }
    return User("tester", "pw", "name", null).withId(10L).apply { addRole(role) }
}

internal fun setUserWithPermission(
    userService: UserService,
    level: PermissionLevel,
    resourceId: String = "c1",
) {
    val user =
        makeUserWithPermissions(
            listOf(
                Permission(
                    resourceName = ResourceType.CCTV.name,
                    resourceId = resourceId,
                    level = level,
                ),
            ),
        )
    every { userService.findUserByUsername("tester") } returns user
}

internal fun initAuthUser(
    userService: UserService,
    roleGlobalPolicyRepository: RoleGlobalPolicyRepository,
) {
    val user = makeUserWithPermissions(emptyList())
    SecurityContextHolder.getContext().authentication =
        UsernamePasswordAuthenticationToken("tester", null, emptyList())
    every { userService.findUserByUsername("tester") } returns user
    every {
        roleGlobalPolicyRepository.existsByRoleIdInAndResourceTypeAndPermissionTypeIn(
            any(),
            any(),
            any(),
        )
    } returns false
}
