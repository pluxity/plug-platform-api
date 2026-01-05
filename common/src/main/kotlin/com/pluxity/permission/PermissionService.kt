package com.pluxity.permission

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.permission.dto.PermissionCreateRequest
import com.pluxity.permission.dto.PermissionResponse
import com.pluxity.permission.dto.PermissionUpdateRequest
import com.pluxity.permission.dto.toPermissionResponse
import com.pluxity.user.repository.RolePermissionRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository,
    private val resourcePermissionRepository: ResourcePermissionRepository,
    private val domainPermissionRepository: DomainPermissionRepository,
    private val rolePermissionRepository: RolePermissionRepository,
) {
    @Transactional
    fun create(request: PermissionCreateRequest): Long {
        if (permissionRepository.existsByName(request.name)) {
            throw CustomException(ErrorCode.DUPLICATE_PERMISSION_NAME, request.name)
        }

        val permission = Permission(name = request.name, description = request.description)
        val requestedResourceKeys = mutableSetOf<String>()
        val requestedDomainKeys = mutableSetOf<String>()
        request.permissions.forEach { permissionRequest ->
            val resourceType = ResourceType.fromString(permissionRequest.resourceType)
            val resourceName = resourceType.name
            val resourceIds = permissionRequest.resourceIds
            val level = permissionRequest.level
            val hasDomainRequest = resourceIds.isEmpty()

            if (hasDomainRequest) {
                if (!requestedDomainKeys.add(resourceName)) {
                    throw CustomException(
                        ErrorCode.DUPLICATE_RESOURCE_ID,
                        "리소스 타입 '$resourceName'에 중복된 도메인 권한이 포함되어 있습니다.",
                    )
                }
                val domainPermission =
                    DomainPermission(
                        resourceName = resourceName,
                        level = level,
                        permission = null,
                    )
                permission.addDomainPermission(domainPermission)
            }

            resourceIds
                .forEach { id ->
                    val key = "$resourceName:$id"
                    if (!requestedResourceKeys.add(key)) {
                        throw CustomException(
                            ErrorCode.DUPLICATE_RESOURCE_ID,
                            "리소스 타입 '$resourceName'에 중복된 ID가 포함되어 있습니다.",
                        )
                    }
                    val resourcePermission =
                        ResourcePermission(
                            resourceName = resourceName,
                            resourceId = id,
                            level = level,
                            permission = null,
                        )
                    permission.addResourcePermission(resourcePermission)
                }
        }

        return permissionRepository.save(permission).requiredId
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): PermissionResponse = findPermissionById(id).toPermissionResponse()

    @Transactional(readOnly = true)
    fun findAll(): List<PermissionResponse> = permissionRepository.findAll().map { it.toPermissionResponse() }

    @Transactional
    fun update(
        id: Long,
        request: PermissionUpdateRequest,
    ) {
        val permission = findPermissionById(id)

        request.name?.takeIf { it.isNotBlank() && it != permission.name }?.let { newName ->
            if (permissionRepository.existsByNameAndIdNot(newName, id)) {
                throw CustomException(ErrorCode.DUPLICATE_PERMISSION_NAME, newName)
            }
            permission.changeName(newName)
        }

        request.description?.let { permission.changeDescription(it) }

        // 현재 권한을 "ResourceType:ResourceId:Level" 형태의 키를 가진 Map으로 변환
        val existingResourceMap =
            permission.resourcePermissions
                .associateBy { "${it.resourceName}:${it.resourceId}" }
        val existingDomainMap =
            permission.domainPermissions
                .associateBy { it.resourceName }

        val requestedResourceMap = mutableMapOf<String, PermissionLevel>()
        val requestedDomainMap = mutableMapOf<String, PermissionLevel>()
        request.permissions.forEach { permissionRequest ->
            val resourceName = ResourceType.fromString(permissionRequest.resourceType).name
            val level = permissionRequest.level
            val resourceIds = permissionRequest.resourceIds
            val hasDomainRequest = resourceIds.isEmpty()

            if (hasDomainRequest) {
                val previous = requestedDomainMap.put(resourceName, level)
                if (previous != null && previous != level) {
                    throw CustomException(
                        ErrorCode.DUPLICATE_RESOURCE_ID,
                        "리소스 타입 '$resourceName'에 중복된 도메인 권한이 포함되어 있습니다.",
                    )
                }
            }

            resourceIds
                .forEach { resourceId ->
                    val key = "$resourceName:$resourceId"
                    val previous = requestedResourceMap.put(key, level)
                    if (previous != null && previous != level) {
                        throw CustomException(
                            ErrorCode.DUPLICATE_RESOURCE_ID,
                            "리소스 타입 '$resourceName'에 중복된 ID가 포함되어 있습니다.",
                        )
                    }
                }
        }

        existingDomainMap
            .filterKeys { it !in requestedDomainMap.keys }
            .values
            .forEach { domainPermission ->
                permission.removeDomainPermission(domainPermission)
                domainPermissionRepository.delete(domainPermission)
            }

        existingResourceMap
            .filterKeys { it !in requestedResourceMap.keys }
            .values
            .forEach { resourcePermission ->
                permission.removeResourcePermission(resourcePermission)
                resourcePermissionRepository.delete(resourcePermission)
            }

        // 도메인 권한 업데이트 및 추가
        request.permissions.forEach { permissionRequest ->
            val resourceName = ResourceType.fromString(permissionRequest.resourceType).name
            val level = permissionRequest.level
            val resourceIds = permissionRequest.resourceIds
            val hasDomainRequest = resourceIds.isEmpty()

            if (hasDomainRequest) {
                val existingDomain = existingDomainMap[resourceName]
                if (existingDomain == null) {
                    val newPermission = DomainPermission(resourceName = resourceName, level = level)
                    permission.addDomainPermission(newPermission)
                } else if (existingDomain.level != level) {
                    existingDomain.level = level
                }
            }

            resourceIds
                .forEach { resourceId ->
                    val key = "$resourceName:$resourceId"
                    val existingResource = existingResourceMap[key]
                    if (existingResource == null) {
                        val newPermission =
                            ResourcePermission(resourceName = resourceName, resourceId = resourceId, level = level)
                        permission.addResourcePermission(newPermission)
                    } else if (existingResource.level != level) {
                        existingResource.level = level
                    }
                }
        }
    }

    @Transactional
    fun delete(id: Long) {
        val permission = findPermissionById(id)
        with(permission) {
            rolePermissionRepository.deleteAllByPermission(this)
            resourcePermissionRepository.deleteAll(permission.resourcePermissions)
            domainPermissionRepository.deleteAll(permission.domainPermissions)
        }
        permissionRepository.delete(permission)
    }

    fun findPermissionById(id: Long): Permission =
        permissionRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_PERMISSION, id)
}
