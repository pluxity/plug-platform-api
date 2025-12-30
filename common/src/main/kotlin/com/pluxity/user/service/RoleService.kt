package com.pluxity.user.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.PermissionGroupService
import com.pluxity.user.dto.RoleCreateRequest
import com.pluxity.user.dto.RoleGlobalPolicyRequest
import com.pluxity.user.dto.RoleResponse
import com.pluxity.user.dto.RoleUpdateRequest
import com.pluxity.user.dto.toRoleResponse
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RoleGlobalPolicy
import com.pluxity.user.entity.RolePermission
import com.pluxity.user.entity.RoleType
import com.pluxity.user.repository.RoleGlobalPolicyRepository
import com.pluxity.user.repository.RolePermissionRepository
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRoleRepository
import jakarta.persistence.EntityManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val rolePermissionRepository: RolePermissionRepository,
    private val userRoleRepository: UserRoleRepository,
    private val permissionGroupService: PermissionGroupService,
    private val roleGlobalPolicyRepository: RoleGlobalPolicyRepository,
    private val em: EntityManager,
) {
    @Transactional
    fun save(
        request: RoleCreateRequest,
        authentication: Authentication,
    ): Long {
        if (request.authority == RoleType.ADMIN && authentication.authorities.none { it.authority == "ROLE_${RoleType.ADMIN.name}" }) {
            throw CustomException(ErrorCode.PERMISSION_DENIED)
        }
        val role =
            roleRepository.save(
                Role(
                    name = request.name,
                    description = request.description,
                    auth = request.authority.name,
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

        if (request.globalPolicies.isNotEmpty()) {
            val policies =
                request.globalPolicies.map { policy ->
                    RoleGlobalPolicy(
                        role = role,
                        resourceType = policy.resourceType,
                        permissionType = policy.permissionType,
                    )
                }
            roleGlobalPolicyRepository.saveAll(policies)
        }

        return role.requiredId
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
        request.globalPolicies?.let { syncGlobalPolicies(role, it) }
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

    private fun syncGlobalPolicies(
        role: Role,
        requestedPolicies: List<RoleGlobalPolicyRequest>,
    ) {
        val requestedKeys =
            requestedPolicies
                .map { it.resourceType to it.permissionType }
                .toSet()
        val existingPolicies = roleGlobalPolicyRepository.findAllByRoleId(role.requiredId)

        val toRemove =
            existingPolicies.filter {
                (it.resourceType to it.permissionType) !in requestedKeys
            }
        if (toRemove.isNotEmpty()) {
            roleGlobalPolicyRepository.deleteAllInBatch(toRemove)
        }

        val toAdd =
            requestedPolicies.filter { requested ->
                existingPolicies.none {
                    it.resourceType == requested.resourceType &&
                        it.permissionType == requested.permissionType
                }
            }
        if (toAdd.isNotEmpty()) {
            val newPolicies =
                toAdd.map { policy ->
                    RoleGlobalPolicy(
                        role = role,
                        resourceType = policy.resourceType,
                        permissionType = policy.permissionType,
                    )
                }
            roleGlobalPolicyRepository.saveAll(newPolicies)
        }
    }

    @Transactional
    fun delete(id: Long) {
        val role = findRoleById(id)
        roleGlobalPolicyRepository.deleteAllInBatch(roleGlobalPolicyRepository.findAllByRoleId(role.requiredId))
        rolePermissionRepository.deleteAllByRole(role)
        userRoleRepository.deleteAllByRole(role)
        em.flush()
        em.clear()
        roleRepository.deleteById(role.requiredId)
    }

    fun findRoleById(id: Long): Role =
        roleRepository.findWithInfoById(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_ROLE, id)
}
