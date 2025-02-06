package com.pluxity.user.service;

import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.Template;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TemplateService templateService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(findUserById(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        return UserResponse.from(
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new EntityNotFoundException("User not found with username: " + username)));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getUserRoles(Long userId) {
        User user = findUserById(userId);
        return user.getRoles().stream().map(RoleResponse::from).toList();
    }

    @Transactional
    public UserResponse save(UserCreateRequest request) {
        User user =
                User.builder()
                        .username(request.username())
                        .password(passwordEncoder.encode(request.password()))
                        .name(request.name())
                        .code(request.code())
                        .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = findUserById(id);
        updateUserFields(user, request);
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse assignRolesToUser(Long userId, UserRoleAssignRequest request) {
        User user = findUserById(userId);
        List<Role> roles = request.roleIds().stream().map(this::findRoleById).toList();

        user.addRoles(roles);
        return UserResponse.from(user);
    }

    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        User user = findUserById(userId);
        Role role = findRoleById(roleId);
        user.removeRole(role);
    }

    @Transactional
    public UserResponse assignTemplateToUser(Long userId, Long templateId) {
        User user = findUserById(userId);
        Template template = templateService.findTemplateById(templateId);
        user.changeTemplate(template);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getUserTemplate(Long userId) {
        User user = findUserById(userId);
        Template template = user.getTemplate();
        if (template == null) {
            throw new EntityNotFoundException("Template not found for user with id: " + userId);
        }
        return TemplateResponse.from(template);
    }

    @Transactional
    public UserResponse updateUserTemplate(Long userId, TemplateUpdateRequest request) {
        User user = findUserById(userId);
        Template template = user.getTemplate();
        if (template == null) {
            throw new EntityNotFoundException("Template not found for user with id: " + userId);
        }

        if (request.name() != null && !request.name().isBlank()) {
            template.changeName(request.name());
        }
        if (request.url() != null && !request.url().isBlank()) {
            template.changeUrl(request.url());
        }

        return UserResponse.from(user);
    }

    @Transactional
    public void removeUserTemplate(Long userId) {
        User user = findUserById(userId);
        user.removeTemplate();
    }

    private User findUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    private Role findRoleById(Long id) {
        return roleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));
    }

    private void updateUserFields(User user, UserUpdateRequest request) {
        if (request.username() != null && !request.username().isBlank()) {
            user.changeUsername(request.username());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.changePassword(passwordEncoder.encode(request.password()));
        }
        if (request.name() != null && !request.name().isBlank()) {
            user.changeName(request.name());
        }
        if (request.code() != null && !request.code().isBlank()) {
            user.changeCode(request.code());
        }
        if (request.templateId() != null) {
            Template template = templateService.findTemplateById(request.templateId());
            user.changeTemplate(template);
        }
    }
}
