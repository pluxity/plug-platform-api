package com.pluxity.user.service;

import com.pluxity.TestApplication;
import com.pluxity.TestAuditingConfig;
import com.pluxity.user.dto.PermissionCreateRequest;
import com.pluxity.user.dto.PermissionResponse;
import com.pluxity.user.dto.PermissionUpdateRequest;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.repository.PermissionRepository;
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
class PermissionServiceTest {

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private EntityManager em;


    @Test
    @DisplayName("모든 권한을 조회할 수 있다")
    void findAll() {
        // given
        Permission permission1 = Permission.builder()
                .name("READ_USER")
                .description("READ_USER")
                .build();
        Permission permission2 = Permission.builder()
                .name("WRITE_USER")
                .description("WRITE_USER")
                .build();
        
        permissionRepository.save(permission1);
        permissionRepository.save(permission2);

        // when
        List<PermissionResponse> result = permissionService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("description")
                .containsExactlyInAnyOrder("READ_USER", "WRITE_USER");
    }

    @Test
    @DisplayName("ID로 권한을 조회할 수 있다")
    void findById() {
        // given
        Permission permission = Permission.builder()
                .name("READ_USER")
                .description("READ_USER")
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        // when
        PermissionResponse result = permissionService.findById(savedPermission.getId());

        // then
        assertThat(result.id()).isEqualTo(savedPermission.getId());
        assertThat(result.description()).isEqualTo("READ_USER");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회시 예외가 발생한다")
    void findById_NotFound() {
        // given
        Long id = 999L;

        // when & then
        assertThatThrownBy(() -> permissionService.findById(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Permission not found");
    }

    @Test
    @DisplayName("새로운 권한을 생성할 수 있다")
    void save() {
        // given
        PermissionCreateRequest request = new PermissionCreateRequest("READ_USER", "DESC");

        // when
        PermissionResponse result = permissionService.save(request);

        // then
        assertThat(result.id()).isNotNull();

        Permission savedPermission = permissionRepository.findById(result.id()).orElseThrow();
        assertThat(savedPermission.getName()).isEqualTo("READ_USER");
        assertThat(savedPermission.getDescription()).isEqualTo("DESC");
    }

    @Test
    @DisplayName("권한 정보를 수정할 수 있다")
    void update() {
        // given
        Permission permission = Permission.builder()
                .name("OLD_PERMISSION")
                .description("OLD_PERMISSION")
                .build();
        Permission savedPermission = permissionRepository.save(permission);
        
        PermissionUpdateRequest request = new PermissionUpdateRequest("UPDATED_PERMISSION", "DESC");

        // when
        PermissionResponse result = permissionService.update(savedPermission.getId(), request);

        // then
        Permission updatedPermission = permissionRepository.findById(savedPermission.getId()).orElseThrow();
        assertThat(updatedPermission.getName()).isEqualTo("UPDATED_PERMISSION");
        assertThat(updatedPermission.getDescription()).isEqualTo("DESC");
    }

    @Test
    @DisplayName("존재하지 않는 권한 수정시 예외가 발생한다")
    void update_NotFound() {
        // given
        Long id = 999L;
        PermissionUpdateRequest request = new PermissionUpdateRequest("UPDATED_PERMISSION", "");

        // when & then
        assertThatThrownBy(() -> permissionService.update(id, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Permission not found");
    }

    @Test
    @DisplayName("권한을 삭제할 수 있다")
    void delete() {
        // given
        Permission permission = Permission.builder()
                .name("TO_BE_DELETED")
                .description("TO_BE_DELETED")
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        // when
        permissionService.delete(savedPermission.getId());

        // then
        assertThat(permissionRepository.findById(savedPermission.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 권한 삭제시 예외가 발생한다")
    void delete_NotFound() {
        // given
        Long id = 999L;

        // when & then
        assertThatThrownBy(() -> permissionService.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Permission not found");
    }
}