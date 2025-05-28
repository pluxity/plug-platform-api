package com.pluxity.user.service;

import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.RoleCreateRequest;
import com.pluxity.user.dto.RoleResponse;
import com.pluxity.user.dto.RoleUpdateRequest;
import com.pluxity.user.entity.Role;
import com.pluxity.user.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        roleRepository
                .findByName(request.name())
                .ifPresent(
                        existingRole -> {
                            throw new CustomException(
                                    "Role already exists with name",
                                    HttpStatus.BAD_REQUEST,
                                    "이미 Role에 존재하는 이름입니다: " + request.name());
                        });
        Role savedRole = roleRepository.save(role);
        return RoleResponse.from(savedRole);
    }

    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        Role role = findRoleById(id);

        if (request.name() != null && !request.name().isBlank()) {
            role.changeRoleName(request.name());
        }
        if (request.description() != null) {
            role.changeDescription(request.description());
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
