package com.pluxity.user.service;

import com.pluxity.TestApplication;
import com.pluxity.TestAuditingConfig;
import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.entity.Role;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {TestApplication.class, TestAuditingConfig.class})
@Transactional
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("모든 역할을 조회할 수 있다")
    void findAll() {
        // given
        Role role1 = Role.builder()
                .roleName("ADMIN")
                .build();
        Role role2 = Role.builder()
                .roleName("USER")
                .build();

        roleRepository.save(role1);
        roleRepository.save(role2);

        // when
        List<RoleResponse> result = roleService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("roleName")
                .containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("ID로 역할을 조회할 수 있다")
    void findById() {
        // given
        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        // when
        RoleResponse result = roleService.findById(savedRole.getId());

        // then
        assertThat(result.id()).isEqualTo(savedRole.getId());
        assertThat(result.roleName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회시 예외가 발생한다")
    void findById_NotFound() {
        // given
        Long id = 999L;

        // when & then
        assertThatThrownBy(() -> roleService.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("새로운 역할을 생성할 수 있다")
    void save() {
        // given
        RoleCreateRequest request = new RoleCreateRequest("ADMIN");

        // when
        RoleResponse result = roleService.save(request);

        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.roleName()).isEqualTo("ADMIN");

        Role savedRole = roleRepository.findById(result.id()).orElseThrow();
        assertThat(savedRole.getRoleName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("역할의 권한을 업데이트할 수 있다")
    void updatePermissions() {
        // given
        Role role = Role.builder()
                .roleName("ROLE_TEST")
                .build();
        Role savedRole = roleRepository.save(role);

        // 기존 권한 생성 및 할당
        Permission permission1 = Permission.builder()
                .name("PERMISSION_1")
                .description("PERMISSION_1")
                .build();
        Permission permission2 = Permission.builder()
                .name("PERMISSION_2")
                .description("PERMISSION_2")
                .build();
        Permission permission3 = Permission.builder()
                .name("PERMISSION_3")
                .description("PERMISSION_3")
                .build();
        permissionRepository.saveAll(List.of(permission1, permission2, permission3));

        // 초기 권한 할당
        savedRole.addPermissions(List.of(permission1, permission2));
        em.flush();
        em.clear();

        // permission1은 유지, permission2는 제거, permission3는 새로 추가
        RolePermissionUpdateRequest request = new RolePermissionUpdateRequest(
                List.of(permission1.getId(), permission3.getId())
        );

        // when
        RoleResponse result = roleService.updatePermissions(savedRole.getId(), request);
        em.flush();
        em.clear();

        // then
        Role updatedRole = roleRepository.findById(savedRole.getId()).orElseThrow();
        List<String> permissionDescriptions = updatedRole.getPermissions().stream()
                .map(Permission::getDescription)
                .sorted()
                .toList();

        assertThat(permissionDescriptions).hasSize(2);
        assertThat(permissionDescriptions).containsExactly("PERMISSION_1", "PERMISSION_3");
    }

    @Test
    @DisplayName("모든 권한을 제거할 수 있다")
    void updatePermissions_Clear() {
        // given
        Role role = Role.builder()
                .roleName("ROLE_TEST")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission1 = Permission.builder()
                .name("PERMISSION_1")
                .description("PERMISSION_1")
                .build();
        Permission permission2 = Permission.builder()
                .name("PERMISSION_2")
                .description("PERMISSION_2")
                .build();
        permissionRepository.saveAll(List.of(permission1, permission2));

        savedRole.addPermissions(List.of(permission1, permission2));
        em.flush();
        em.clear();

        RolePermissionUpdateRequest request = new RolePermissionUpdateRequest(List.of());

        // when
        RoleResponse result = roleService.updatePermissions(savedRole.getId(), request);
        em.flush();
        em.clear();

        // then
        Role updatedRole = roleRepository.findById(savedRole.getId()).orElseThrow();
        assertThat(updatedRole.getPermissions()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 역할의 권한을 업데이트할 때 예외가 발생한다")
    void updatePermissions_RoleNotFound() {
        // given
        Long notFoundId = 999L;
        RolePermissionUpdateRequest request = new RolePermissionUpdateRequest(List.of(1L));

        // when & then
        assertThatThrownBy(() -> roleService.updatePermissions(notFoundId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("존재하지 않는 권한으로 업데이트할 때 예외가 발생한다")
    void updatePermissions_PermissionNotFound() {
        // given
        Role role = Role.builder()
                .roleName("ROLE_TEST")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder()
                .name("PERMISSION")
                .description("PERMISSION")
                .build();
        permissionRepository.save(permission);
        savedRole.addPermission(permission);
        em.flush();
        em.clear();

        RolePermissionUpdateRequest request = new RolePermissionUpdateRequest(List.of(999L));

        // when & then
        assertThatThrownBy(() -> roleService.updatePermissions(savedRole.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Some permissions were not found");
    }

    @Test
    @DisplayName("역할 정보를 수정할 수 있다")
    void update() {
        // given
        Role role = Role.builder()
                .roleName("OLD_ROLE")
                .build();
        Role savedRole = roleRepository.save(role);

        RoleUpdateRequest request = RoleUpdateRequest.builder()
                .roleName("UPDATED_ROLE")
                .build();

        // when
        RoleResponse result = roleService.update(savedRole.getId(), request);

        // then
        assertThat(result.roleName()).isEqualTo("UPDATED_ROLE");

        Role updatedRole = roleRepository.findById(savedRole.getId()).orElseThrow();
        assertThat(updatedRole.getRoleName()).isEqualTo("UPDATED_ROLE");
    }

    @Test
    @DisplayName("역할을 삭제할 수 있다")
    void delete() {
        // given
        Role role = Role.builder()
                .roleName("TO_BE_DELETED")
                .build();
        Role savedRole = roleRepository.save(role);

        // when
        roleService.delete(savedRole.getId());

        // then
        assertThat(roleRepository.findById(savedRole.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 역할 삭제시 예외가 발생한다")
    void delete_NotFound() {
        // given
        Long id = 999L;

        // when & then
        assertThatThrownBy(() -> roleService.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Role not found");
    }

    @Test
    @DisplayName("역할에 권한을 할당할 수 있다")
    void assignPermissionsToRole() {
        // given
        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission1 = Permission.builder()
                .name("READ_USER")
                .description("READ_USER")
                .build();
        Permission permission2 = Permission.builder()
                .name("WRITE_USER")
                .description("WRITE_USER")
                .build();
        permissionRepository.saveAll(List.of(permission1, permission2));

        RolePermissionAssignRequest request = new RolePermissionAssignRequest(
                List.of(permission1.getId(), permission2.getId())
        );

        // when
        RoleResponse result = roleService.assignPermissionsToRole(savedRole.getId(), request);

        // then
        assertThat(result.permissions()).hasSize(2);
        assertThat(result.permissions()).extracting("description")
                .containsExactlyInAnyOrder("READ_USER", "WRITE_USER");

        Role updatedRole = roleRepository.findById(savedRole.getId()).orElseThrow();
        assertThat(updatedRole.getPermissions()).hasSize(2);
        assertThat(updatedRole.getPermissions()).extracting("description")
                .containsExactlyInAnyOrder("READ_USER", "WRITE_USER");
    }

    @Test
    @DisplayName("역할의 권한 목록을 조회할 수 있다")
    void getRolePermissions() {
        // given
        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder()
                .name("READ_USER")
                .description("READ_USER")
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        savedRole.addPermission(savedPermission);
        roleRepository.saveAndFlush(savedRole);
        em.clear();

        // when
        List<PermissionResponse> result = roleService.getRolePermissions(savedRole.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).description()).isEqualTo("READ_USER");
    }

    @Test
    @DisplayName("이미 할당된 권한을 다시 할당하려 할 때 예외가 발생한다")
    void assignPermissionsToRole_AlreadyExists() {
        // given
        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder()
                .name("READ_USER")
                .description("READ_USER")
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        savedRole.addPermission(savedPermission);
        em.flush();
        em.clear();

        RolePermissionAssignRequest request = new RolePermissionAssignRequest(List.of(savedPermission.getId()));

        // when & then
        assertThatThrownBy(() -> roleService.assignPermissionsToRole(savedRole.getId(), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Some permissions already exist in this role");
    }

    @Test
    @DisplayName("역할에서 권한을 제거할 수 있다")
    void removePermissionFromRole() {
        // given
        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder()
                .name("READ_USER")
                .description("READ_USER")
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        savedRole.addPermission(savedPermission);
        em.flush();
        em.clear();

        // when
        roleService.removePermissionFromRole(savedRole.getId(), savedPermission.getId());

        // then
        Role updatedRole = roleRepository.findById(savedRole.getId()).orElseThrow();
        assertThat(updatedRole.getPermissions()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 권한을 제거하려 할 때 예외가 발생한다")
    void removePermissionFromRole_NotFound() {
        // given
        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder()
                .name("READ_USER")
                .description("READ_USER")
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        // when & then
        assertThatThrownBy(() -> roleService.removePermissionFromRole(savedRole.getId(), savedPermission.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Permission not found for this role");
    }

}