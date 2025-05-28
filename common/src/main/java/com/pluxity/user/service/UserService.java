package com.pluxity.user.service;

import com.pluxity.authentication.entity.RefreshToken;
import com.pluxity.authentication.repository.RefreshTokenRepository;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

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
                        .phoneNumber(request.phoneNumber())
                        .department(request.department())
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

    @Transactional
    public void updateUserPassword(Long id, UserPasswordUpdateRequest request) {

        User user = findUserById(id);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException(
                    "Invalid current password", HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateUserRoles(Long id, UserRoleUpdateRequest request) {
        User user = findUserById(id);
        List<Role> roles = roleRepository.findAllById(request.roleIds());
        user.updateRoles(roles);
        return UserResponse.from(user);
    }

    private void updateUserFields(User user, UserUpdateRequest request) {
        if (request.username() != null && !request.username().isBlank()) {
            user.changeUsername(request.username());
        }
        if (request.name() != null && !request.name().isBlank()) {
            user.changeName(request.name());
        }
        if (request.code() != null && !request.code().isBlank()) {
            user.changeCode(request.code());
        }
        if (request.phoneNumber() != null) {
            user.changePhoneNumber(request.phoneNumber());
        }
        if (request.department() != null) {
            user.changeDepartment(request.department());
        }
    }

    @Transactional(readOnly = true)
    public List<UserLoggedInResponse> isLoggedIn() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(
                        user -> {
                            Optional<RefreshToken> refreshToken =
                                    refreshTokenRepository.findById(user.getUsername());
                            boolean isLoggedIn = refreshToken.isPresent();
                            return UserLoggedInResponse.from(
                                    user.getId(),
                                    user.getUsername(),
                                    user.getName(),
                                    user.getCode(),
                                    user.getPhoneNumber(),
                                    user.getDepartment(),
                                    isLoggedIn,
                                    user.getRoles().stream().map(RoleResponse::from).toList());
                        })
                .toList();
    }
}
