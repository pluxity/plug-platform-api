package com.pluxity.user.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionGroupService
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleResponse
import com.pluxity.user.dto.RoleUpdateRequest
import com.pluxity.user.dto.toRoleResponse
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RolePermission
import com.pluxity.user.entity.RoleType
import com.pluxity.user.repository.RolePermissionRepository
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRoleRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val userRoleRepository: UserRoleRepository,
    private val permissionGroupService: PermissionGroupService,
    private val em: EntityManager,
) {
    @Transactional
    fun save(
        request: RoleCreateRequest,
        authentication: Authentication,
    ): Long {
        if (request.auth == RoleType.ADMIN && authentication.authorities.none { it.authority == "ROLE_${RoleType.ADMIN.name}" }) {
            throw CustomException(ErrorCode.PERMISSION_DENIED)
        }
        val role =
            roleRepository.save(
                Role(
                    name = request.name,
                    description = request.description,
                    auth = request.auth.name,
                ),
            )

        request.permissionGroupIds.let { groupIds ->
            if (groupIds.isNotEmpty()) {
                val newRolePermissions =
                    groupIds.map { groupId ->
                        val permissionGroup = permissionGroupService.findPermissionGroupById(groupId)
                        RolePermission(
                            role = role,
                            permissionGroup = permissionGroup,
                        )
                    }

                rolePermissionRepository.saveAll(newRolePermissions)
                newRolePermissions.forEach { rolePermission ->
                    role.addRolePermission(rolePermission)
                }
            }
        }

        return role.id!!
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): RoleResponse = findRoleById(id).toRoleResponse()

    @Transactional(readOnly = true)
    fun findAll(): List<RoleResponse> =
        roleRepository
            .findByAuthIsNotOrderByCreatedAtDesc("ADMIN")
            .map { it.toRoleResponse() }

    @Transactional
    fun update(
        id: Long,
        request: RoleUpdateRequest,
    ) {
        val role = findRoleById(id)

        request.name?.takeIf { it.isNotBlank() }?.let {
            role.changeRoleName(it)
        }
        request.description?.let { role.changeDescription(request.description) }

        request.permissionGroupIds?.let { syncPermissionGroups(role, request.permissionGroupIds) }
    }

    private fun syncPermissionGroups(
        role: Role,
        requestedGroupIds: List<Long>,
    ) {
        val currentGroupIds = role.rolePermissions.map { it.permissionGroup.id }.toSet()
        val requestedGroupIdsSet = requestedGroupIds.toSet()

        val rolePermissionsToRemove =
            role.rolePermissions
                .filter { !requestedGroupIdsSet.contains(it.permissionGroup.id) }

        if (rolePermissionsToRemove.isNotEmpty()) {
            rolePermissionRepository.deleteAllInBatch(rolePermissionsToRemove)
            rolePermissionsToRemove.forEach { rolePermission ->
                role.removeRolePermission(rolePermission)
            }
        }

        val idsToAdd = requestedGroupIdsSet.filter { !currentGroupIds.contains(it) }

        if (idsToAdd.isNotEmpty()) {
            val rolePermissionsToAdd =
                idsToAdd.map { groupId ->
                    val permissionGroup = permissionGroupService.findPermissionGroupById(groupId)
                    RolePermission(
                        role = role,
                        permissionGroup = permissionGroup,
                    )
                }

            rolePermissionRepository.saveAll(rolePermissionsToAdd).forEach { rolePermission ->
                role.addRolePermission(rolePermission)
            }
        }
    }

    @Transactional
    fun delete(id: Long) {
        val role = findRoleById(id)
        rolePermissionRepository.deleteAllByRole(role)
        userRoleRepository.deleteAllByRole(role)
        em.flush()
        em.clear()
        roleRepository.deleteById(role.id!!)
    }

    fun findRoleById(id: Long): Role =
        roleRepository.findWithInfoById(id)
            ?: throw EntityNotFoundException("Role not found with id: $id")
}
