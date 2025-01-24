package com.pluxity.user.service;

import com.pluxity.user.dto.RequestPermission;
import com.pluxity.user.dto.ResponsePermission;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return permissionRepository
                .findById(id)
                .map(ResponsePermission::from)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));
    }

    @Transactional
    public ResponsePermission save(RequestPermission request) {
        Permission permission = Permission.builder().description(request.description()).build();

        return ResponsePermission.from(permissionRepository.save(permission));
    }

    @Transactional
    public ResponsePermission update(Long id, RequestPermission request) {
        Permission permission =
                permissionRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));

        permission.updateDescription(request.description());
        return ResponsePermission.from(permission);
    }

    @Transactional
    public void delete(Long id) {
        Permission permission =
                permissionRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));

        permissionRepository.delete(permission);
    }
}
