package com.pluxity.user.service;

import com.pluxity.user.dto.PermissionCreateRequest;
import com.pluxity.user.dto.PermissionResponse;
import com.pluxity.user.dto.PermissionUpdateRequest;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll().stream().map(PermissionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PermissionResponse findById(Long id) {
        return PermissionResponse.from(findPermissionById(id));
    }

    @Transactional
    public PermissionResponse save(PermissionCreateRequest request) {
        Permission permission =
                Permission.builder().name(request.name()).description(request.description()).build();

        return PermissionResponse.from(permissionRepository.save(permission));
    }

    @Transactional
    public PermissionResponse update(Long id, PermissionUpdateRequest request) {
        Permission permission = findPermissionById(id);
        if (request.name() != null && !request.name().isBlank()) {
            permission.changeName(request.name());
        }
        if (request.description() != null && !request.description().isBlank()) {
            permission.changeDescription(request.description());
        }
        return PermissionResponse.from(permission);
    }

    @Transactional
    public void delete(Long id) {
        Permission permission = findPermissionById(id);
        permissionRepository.delete(permission);
    }

    private Permission findPermissionById(Long id) {
        return permissionRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));
    }
}
