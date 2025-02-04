package com.pluxity.user.service;

import com.pluxity.user.dto.RequestUser;
import com.pluxity.user.dto.RequestUserRoles;
import com.pluxity.user.dto.ResponseRole;
import com.pluxity.user.dto.ResponseUser;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ResponseUser findById(Long id) {
        return ResponseUser.from(findUserById(id));
    }

    @Transactional(readOnly = true)
    public List<ResponseUser> findAll() {
        return userRepository.findAll().stream().map(ResponseUser::from).toList();
    }

    @Transactional(readOnly = true)
    public ResponseUser findByUsername(String username) {
        return ResponseUser.from(
                userRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new EntityNotFoundException("User not found with username: " + username)));
    }

    @Transactional(readOnly = true)
    public List<ResponseRole> getUserRoles(Long userId) {
        User user = findUserById(userId);
        return user.getRoles().stream().map(ResponseRole::from).toList();
    }

    @Transactional
    public ResponseUser save(RequestUser request) {
        User user =
                User.builder()
                        .username(request.username())
                        .password(passwordEncoder.encode(request.password()))
                        .name(request.name())
                        .code(request.code())
                        .build();

        User savedUser = userRepository.save(user);
        return ResponseUser.from(savedUser);
    }

    @Transactional
    public ResponseUser update(Long id, RequestUser request) {
        User user = findUserById(id);
        updateUserFields(user, request);
        return ResponseUser.from(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    @Transactional
    public ResponseUser assignRolesToUser(Long userId, RequestUserRoles request) {
        User user = findUserById(userId);
        List<Role> roles = request.roleIds().stream().map(this::findRoleById).toList();

        user.addRoles(roles);
        return ResponseUser.from(user);
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

    private void updateUserFields(User user, RequestUser request) {
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
    }
}
