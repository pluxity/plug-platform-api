package com.pluxity.user.service;

import com.pluxity.user.dto.RequestPermission;
import com.pluxity.user.dto.ResponsePermission;
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
    public List<ResponsePermission> findAll() {
        return permissionRepository.findAll().stream().map(ResponsePermission::from).toList();
    }

    @Transactional(readOnly = true)
    public ResponsePermission findById(Long id) {
        return ResponsePermission.from(findPermissionById(id));
    }

    @Transactional
    public ResponsePermission save(RequestPermission request) {
        Permission permission = Permission.builder().description(request.description()).build();

        return ResponsePermission.from(permissionRepository.save(permission));
    }

    @Transactional
    public ResponsePermission update(Long id, RequestPermission request) {
        Permission permission = findPermissionById(id);
        permission.changeDescription(request.description());
        return ResponsePermission.from(permission);
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
