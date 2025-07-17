package com.pluxity.user.service;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.PermissionRequest;
import com.pluxity.user.entity.ResourcePermission;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.entity.Role;
import com.pluxity.user.repository.ResourcePermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RoleRepository roleRepository;
    private final ApplicationContext applicationContext;
    private final Map<String, JpaRepository<?, Long>> repositoryMap;
    private final ResourcePermissionRepository permissionRepository;

    @Transactional
    public void grantPermissionToRole(PermissionRequest request) {
        String resourceName = request.resourceName().getResourceName();
        ResourceType resourceType = ResourceType.fromString(resourceName);

        request
                .resourceId()
                .forEach(
                        resourceId -> {
                            validateResourceExists(resourceType, resourceId);

                            Role role =
                                    roleRepository
                                            .findById(request.roleId())
                                            .orElseThrow(
                                                    () ->
                                                            new CustomException(ErrorCode.NOT_FOUND_ROLE, request.resourceId()));

                            if (permissionRepository.existsByRoleAndResourceNameAndResourceId(
                                    role, resourceName, resourceId)) {
                                return;
                            }

                            ResourcePermission newPermission =
                                    ResourcePermission.builder()
                                            .role(role)
                                            .resourceName(resourceName)
                                            .resourceId(resourceId)
                                            .build();
                            permissionRepository.save(newPermission);
                        });
    }

    @Transactional
    public void revokePermissionFromRole(PermissionRequest request) {
        String resourceName = request.resourceName().getResourceName();
        ResourceType resourceType = ResourceType.fromString(resourceName);

        request
                .resourceId()
                .forEach(
                        resourceId -> {
                            validateResourceExists(resourceType, resourceId);
                            Role role =
                                    roleRepository
                                            .findById(request.roleId())
                                            .orElseThrow(
                                                    () ->
                                                            new CustomException(ErrorCode.NOT_FOUND_ROLE, request.resourceId()));

                            permissionRepository.deleteByRoleAndResourceNameAndResourceId(
                                    role, resourceName, resourceId);
                        });
    }

    @Transactional
    public void syncPermissions(PermissionRequest request) {
        ResourceType resourceType = ResourceType.fromString(request.resourceName().getResourceName());
        validateAllResourcesExist(resourceType, request.resourceId());

        Role role =
                roleRepository
                        .findById(request.roleId())
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROLE, request.roleId()));

        List<Long> currentDbResourceIds =
                permissionRepository.findResourceIdsByRoleAndResourceName(
                        role, resourceType.getResourceName());

        List<Long> idsToRemove =
                currentDbResourceIds.stream().filter(id -> !request.resourceId().contains(id)).toList();

        if (!idsToRemove.isEmpty()) {
            permissionRepository.deleteByRoleAndResourceNameAndResourceIdIn(
                    role, resourceType.getResourceName(), idsToRemove);
        }

        List<Long> idsToAdd =
                request.resourceId().stream().filter(id -> !currentDbResourceIds.contains(id)).toList();

        if (!idsToAdd.isEmpty()) {
            List<ResourcePermission> permissionsToSave =
                    idsToAdd.stream()
                            .map(
                                    id ->
                                            ResourcePermission.builder()
                                                    .role(role)
                                                    .resourceName(resourceType.getResourceName())
                                                    .resourceId(id)
                                                    .build())
                            .collect(Collectors.toList());
            permissionRepository.saveAll(permissionsToSave);
        }
    }

    private void validateResourceExists(ResourceType resourceType, Long resourceId) {
        // Enum 이름을 기반으로 리포지토리 빈의 이름을 추론합니다 (예: FACILITY -> facilityRepository)
        String repositoryBeanName = resourceType.name().toLowerCase() + "Repository";

        try {
            JpaRepository<?, Long> repository =
                    (JpaRepository<?, Long>) applicationContext.getBean(repositoryBeanName);
            if (!repository.existsById(resourceId)) {
                throw new CustomException(
                        ErrorCode.NOT_FOUND_RESOURCE, resourceType.getResourceName(), resourceId);
            }
        } catch (Exception e) {
            // 해당 이름의 리포지토리 빈이 없는 경우 등
            throw new IllegalStateException(
                    "Could not find repository for resource type: " + resourceType.getResourceName(), e);
        }
    }

    private void validateAllResourcesExist(ResourceType resourceType, List<Long> resourceIds) {
        // resourceIds가 비어있거나 null이면 검증할 필요가 없음
        if (CollectionUtils.isEmpty(resourceIds)) {
            return;
        }

        // 1. 리포지토리 빈의 이름을 추론합니다 (e.g., FACILITY -> facilityRepository)
        String repositoryBeanName = resourceType.name().toLowerCase() + "Repository";

        try {
            // 2. ApplicationContext에서 리포지토리 빈을 가져옵니다.
            Object repository = applicationContext.getBean(repositoryBeanName);

            // 3. 리플렉션을 사용해 'countByIdIn' 메서드를 찾습니다.
            //    메서드의 파라미터 타입이 List.class임을 명시해줍니다.
            Method countMethod = repository.getClass().getMethod("countByIdIn", List.class);

            // 4. 리플렉션을 사용해 메서드를 호출합니다.
            //    첫 번째 인자는 메서드를 호출할 인스턴스(repository), 두 번째 인자는 전달할 파라미터(resourceIds)입니다.
            long count = (long) countMethod.invoke(repository, resourceIds);

            // 5. 요청된 ID의 개수와 DB에 실제 존재하는 개수를 비교합니다.
            if (count != resourceIds.size()) {
                throw new CustomException(ErrorCode.INVALID_RESOURCE_IDS_INCLUDED, resourceIds);
            }

        } catch (NoSuchMethodException e) {
            // 'countByIdIn' 메서드가 해당 리포지토리에 정의되지 않은 경우
            throw new IllegalStateException(
                    "Repository '"
                            + repositoryBeanName
                            + "' must have a 'countByIdIn(List<Long> ids)' method.",
                    e);
        } catch (Exception e) {
            // 그 외 리포지토리 빈을 찾지 못했거나, 리플렉션 호출에 실패한 경우
            throw new IllegalStateException(
                    "Could not validate resources for type: " + resourceType.getResourceName(), e);
        }
    }
}
