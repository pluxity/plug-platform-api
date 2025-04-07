package com.pluxity.user.service;

import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Role;
import com.pluxity.user.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return RoleResponse.from(findRoleById(id));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(RoleResponse::from).toList();
    }

    @Transactional
    public RoleResponse save(RoleCreateRequest request) {
        Role role = Role.builder().name(request.name()).build();

        Role savedRole = roleRepository.save(role);
        return RoleResponse.from(savedRole);
    }

    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        Role role = findRoleById(id);

        if (request.name() != null && !request.name().isBlank()) {
            role.changeRoleName(request.name());
        }
        return RoleResponse.from(role);
    }

    @Transactional
    public void delete(Long id) {
        Role role = findRoleById(id);
        roleRepository.delete(role);
    }

    private Role findRoleById(Long id) {
        return roleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }
}
