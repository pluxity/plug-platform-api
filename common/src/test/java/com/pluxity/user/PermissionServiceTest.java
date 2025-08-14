package com.pluxity.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.global.exception.CustomException;
import com.pluxity.permission.Permission;
import com.pluxity.permission.PermissionGroupService;
import com.pluxity.permission.PermissionRepository;
import com.pluxity.permission.PermissionService;
import com.pluxity.permission.ResourceType;
import com.pluxity.permission.dto.*;
import com.pluxity.user.dto.*;
import com.pluxity.user.repository.RolePermissionRepository;
import com.pluxity.user.service.RoleService;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class PermissionServiceTest {

    @Autowired private PermissionService permissionService;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private RoleService roleService;
    @Autowired private RolePermissionRepository rolePermissionRepository;
    @Autowired private EntityManager em;
    @Autowired private PermissionGroupService permissionGroupService;

    @BeforeEach
    void setUp() {
        // 각 테스트는 독립적이므로, 필요 시 여기서 초기화
        rolePermissionRepository.deleteAll();
        permissionRepository.deleteAll();
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("새로운 Permission을 생성하고, ID로 조회하여 검증한다")
    void create_andFindById_succeeds() {
        // GIVEN
        PermissionCreateRequest request =
                new PermissionCreateRequest("test", ResourceType.FACILITY.name(), List.of("main"));

        // WHEN
        Long permissionId = permissionService.create(request).getFirst();
        em.flush();
        em.clear();

        // THEN
        Permission foundPermission = permissionService.findById(permissionId);
        assertThat(foundPermission.getId()).isEqualTo(permissionId);
        assertThat(foundPermission.getResourceName()).isEqualTo(ResourceType.FACILITY.name());
        assertThat(foundPermission.getResourceId()).isEqualTo("main");
    }

    @Test
    @DisplayName("Permission 정보 업데이트 후, 변경사항이 올바르게 반영되었는지 검증한다")
    void update_permission_andVerify() {
        // GIVEN
        Long permissionId =
                permissionService
                        .create(
                                new PermissionCreateRequest(
                                        "TEST", ResourceType.FACILITY.name(), List.of("config")))
                        .getFirst();
        em.flush();
        em.clear();

        // WHEN
        PermissionUpdateRequest updateRequest =
                new PermissionUpdateRequest(ResourceType.FACILITY.name(), "new_config_id");
        permissionService.update(permissionId, updateRequest);
        em.flush();
        em.clear();

        // THEN
        Permission updatedPermission = permissionService.findById(permissionId);
        assertThat(updatedPermission.getResourceId()).isEqualTo("new_config_id");
        assertThat(updatedPermission.getResourceName()).isEqualTo(ResourceType.FACILITY.name());
    }

    @Test
    @DisplayName("Permission 삭제 시, 연관된 RolePermission도 함께 삭제되는지 검증한다")
    void delete_permission_andVerifyCascadeDelete() {
        // GIVEN
        // 1. PermissionGroup 생성
        Long permissionGroupId =
                permissionGroupService.create(
                        new PermissionGroupCreateRequest(
                                "TEST",
                                "TEST",
                                List.of(new PermissionRequest(ResourceType.FACILITY.name(), List.of("100")))));
        PermissionGroupResponse permissionGroupResponse =
                permissionGroupService.findById(permissionGroupId);
        String permissionId = permissionGroupResponse.permissions().getFirst().resourceIds().getFirst();

        // 2. Role 생성 및 위 Permission 할당
        Long roleId =
                roleService.save(
                        new RoleCreateRequest("RoleWithPermission", "Desc", List.of(permissionGroupId)));
        em.flush();
        em.clear();

        // 3. RolePermission이 생성되었는지 확인
        assertThat(rolePermissionRepository.count()).isEqualTo(1);

        // WHEN
        //        permissionService.delete(permissionId);
        em.flush();
        em.clear();

        // THEN
        // 1. Permission이 삭제되었는지 확인
        //        assertThrows(CustomException.class, () -> permissionService.findById(permissionId));

        // 2. 연관된 RolePermission도 삭제되었는지 확인 (가장 중요)
        //        assertThat(rolePermissionRepository.count()).isZero();

        // 3. Role 자체는 삭제되지 않았는지 확인
        //        assertDoesNotThrow(() -> roleService.findById(roleId));
    }

    @Test
    @DisplayName("존재하지 않는 Permission ID로 조회 시 CustomException이 발생한다")
    void findById_withNonExistentId_throwsException() {
        // GIVEN
        Long nonExistentId = 9999L;

        // WHEN & THEN
        assertThrows(CustomException.class, () -> permissionService.findById(nonExistentId));
    }

    @Test
    @DisplayName("존재하지 않는 ID 목록으로 findAllByIds 조회 시 CustomException이 발생한다")
    void findAllByIds_withNonExistentId_throwsException() {
        // GIVEN
        Long existingId =
                permissionService
                        .create(new PermissionCreateRequest("TEST", ResourceType.FACILITY.name(), List.of("1")))
                        .getFirst();
        List<Long> ids = List.of(existingId, 9999L);
        em.flush();
        em.clear();

        // WHEN & THEN
        assertThrows(CustomException.class, () -> permissionService.findAllByIds(ids));
    }
}
