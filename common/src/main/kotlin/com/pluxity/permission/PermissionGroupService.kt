package com.pluxity.permission

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.dto.PermissionGroupCreateRequest
import com.pluxity.permission.dto.PermissionGroupResponse
import com.pluxity.permission.dto.PermissionGroupUpdateRequest
import com.pluxity.permission.dto.toPermissionGroupResponse
import com.pluxity.user.repository.RolePermissionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PermissionGroupService(
    private val permissionGroupRepository: PermissionGroupRepository,
    private val permissionRepository: PermissionRepository,
    private val rolePermissionRepository: RolePermissionRepository,
) {
    @Transactional
    fun create(request: PermissionGroupCreateRequest): Long {
        if (permissionGroupRepository.existsByName(request.name)) {
            throw CustomException(ErrorCode.DUPLICATE_PERMISSION_GROUP_NAME, request.name)
        }

        val permissionGroup = PermissionGroup(name = request.name, description = request.description)
        request.permissions.forEach { permissionRequest ->
            val resourceType = ResourceType.fromString(permissionRequest.resourceType)
            val resourceName = resourceType.name
            val resourceIds = permissionRequest.resourceIds

            if (resourceIds.size != resourceIds.toSet().size) {
                throw CustomException(
                    ErrorCode.DUPLICATE_RESOURCE_ID,
                    "리소스 타입 '$resourceName'에 중복된 ID가 포함되어 있습니다.",
                )
            }
            resourceIds.forEach { id ->
                val permission =
                    Permission(
                        resourceName = resourceName,
                        resourceId = id,
                        permissionGroup = null,
                    )
                permissionGroup.addPermission(permission)
            }
        }

        return permissionGroupRepository.save(permissionGroup).id!!
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): PermissionGroupResponse = findPermissionGroupById(id).toPermissionGroupResponse()

    @Transactional(readOnly = true)
    fun findAll(): List<PermissionGroupResponse> = permissionGroupRepository.findAll().map { it.toPermissionGroupResponse() }

    @Transactional
    fun update(
        id: Long,
        request: PermissionGroupUpdateRequest,
    ) {
        val permissionGroup = findPermissionGroupById(id)

        request.name?.takeIf { it.isNotBlank() && it != permissionGroup.name }?.let { newName ->
            if (permissionGroupRepository.existsByNameAndIdNot(newName, id)) {
                throw CustomException(ErrorCode.DUPLICATE_PERMISSION_GROUP_NAME, newName)
            }
            permissionGroup.changeName(newName)
        }

        request.description?.let { permissionGroup.changeDescription(it) }

        // 현재 권한을 "ResourceType:ResourceId" 형태의 키를 가진 Map으로 변환
        val existingPermissionsMap =
            permissionGroup.permissions
                .associateBy { "${it.resourceName}:${it.resourceId}" }

        // 요청된 권한을 "ResourceType:ResourceId" 형태의 키를 가진 Set으로 변환
        val requestedPermissionKeys = mutableSetOf<String>()
        request.permissions.forEach { permissionRequest ->
            val resourceName = ResourceType.fromString(permissionRequest.resourceType).name
            permissionRequest.resourceIds.forEach { resourceId ->
                requestedPermissionKeys.add("$resourceName:$resourceId")
            }
        }

        // 삭제할 권한을 찾아 제거 (현재 맵의 키 목록에는 있지만, 요청된 키 Set에는 없는 권한)
        existingPermissionsMap
            .filterKeys { it !in requestedPermissionKeys }
            .values
            .forEach { permission ->
                permissionGroup.removePermission(permission)
                permissionRepository.delete(permission)
            }

        // 추가할 권한을 찾아 생성 및 추가
        request.permissions.forEach { permissionRequest ->
            val resourceName = ResourceType.fromString(permissionRequest.resourceType).name
            permissionRequest.resourceIds
                .filterNot { resourceId ->
                    "$resourceName:$resourceId" in existingPermissionsMap
                }.forEach { resourceId ->
                    val newPermission = Permission(resourceName = resourceName, resourceId = resourceId)
                    permissionGroup.addPermission(newPermission)
                }
        }
    }

    @Transactional
    fun delete(id: Long) {
        val permissionGroup = findPermissionGroupById(id)
        with(permissionGroup) {
            rolePermissionRepository.deleteAllByPermissionGroup(this)
            permissionRepository.deleteAll(permissionGroup.permissions)
        }
        permissionGroupRepository.delete(permissionGroup)
    }

    fun findPermissionGroupById(id: Long): PermissionGroup =
        permissionGroupRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_PERMISSION_GROUP, id)
}
