package util

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

fun makeUserWithPermissions(
    resourcePermissions: List<ResourcePermission>,
    domainPermissions: List<DomainPermission> = emptyList(),
): User {
    val role = Role(name = "ROLE_USER", description = "role").withId(1L)
    if (resourcePermissions.isNotEmpty() || domainPermissions.isNotEmpty()) {
        val permission = Permission("권한 그룹", null)
        resourcePermissions.forEach { permission.addResourcePermission(it) }
        domainPermissions.forEach { permission.addDomainPermission(it) }
        role.addRolePermission(RolePermission(role = role, permission = permission))
    }
    return User("tester", "pw", "name", null).withId(10L).apply { addRole(role) }
}

fun setUserWithPermission(
    userService: UserService,
    resourceType: ResourceType,
    level: PermissionLevel,
    resourceId: String,
) {
    val user =
        makeUserWithPermissions(
            resourcePermissions =
                listOf(
                    ResourcePermission(
                        resourceName = resourceType.name,
                        resourceId = resourceId,
                        level = level,
                    ),
                ),
            domainPermissions = emptyList(),
        )
    every { userService.findUserByUsername("tester") } returns user
}

fun setUserWithPermission(
    userService: UserService,
    resourceType: ResourceType,
    level: PermissionLevel,
    resourceId: Long,
) {
    val user =
        makeUserWithPermissions(
            resourcePermissions =
                listOf(
                    ResourcePermission(
                        resourceName = resourceType.name,
                        resourceId = resourceId.toString(),
                        level = level,
                    ),
                ),
            domainPermissions = emptyList(),
        )
    every { userService.findUserByUsername("tester") } returns user
}

fun setUserWithPermissions(
    userService: UserService,
    resourcePermissions: List<ResourcePermission>,
    domainPermissions: List<DomainPermission> = emptyList(),
) {
    val user = makeUserWithPermissions(resourcePermissions, domainPermissions)
    every { userService.findUserByUsername("tester") } returns user
}

fun initAuthUser(userService: UserService) {
    val user = makeUserWithPermissions(emptyList(), emptyList())
    SecurityContextHolder.getContext().authentication =
        UsernamePasswordAuthenticationToken("tester", null, emptyList())
    every { userService.findUserByUsername("tester") } returns user
}
