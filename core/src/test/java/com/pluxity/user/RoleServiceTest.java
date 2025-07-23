package com.pluxity.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.building.Building;
import com.pluxity.building.BuildingRepository;
import com.pluxity.user.dto.PermissionRequest;
import com.pluxity.user.dto.RoleCreateRequest;
import com.pluxity.user.dto.RoleResponse;
import com.pluxity.user.dto.RoleUpdateRequest;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.service.RoleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test") // test 프로파일 활성화
class RoleServiceTest {
    @Autowired private RoleService roleService;
    // 검증 단계에서는 Repository 직접 사용을 최소화합니다.
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private EntityManager em;

    private List<Building> buildings = new ArrayList<>();
    private RoleCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        buildings.clear();
        IntStream.rangeClosed(1, 3).forEach(i ->
                buildings.add(buildingRepository.save(Building.builder().name("Building " + i).code("B" + i).build()))
        );

        List<Long> initialPermissionIds = List.of(buildings.get(0).getId(), buildings.get(1).getId());
        PermissionRequest permissionRequest = new PermissionRequest(ResourceType.FACILITY, initialPermissionIds);
        createRequest = new RoleCreateRequest("Test Role", "A role for testing", List.of(permissionRequest));
    }

    @Test
    @DisplayName("새로운 Role을 권한과 함께 생성하고, 생성된 Role을 서비스로 조회하여 검증한다")
    void save_withPermissions_andVerifyWithService() {
        // WHEN
        Long roleId = roleService.save(createRequest);
        em.flush();
        em.clear();

        // THEN: Repository 대신 Service를 통해 조회하여 검증합니다.
        RoleResponse response = roleService.findById(roleId);

        assertThat(response.name()).isEqualTo("Test Role");
        assertThat(response.description()).isEqualTo("A role for testing");

        // DTO의 그룹화된 권한 목록을 검증합니다.
        assertThat(response.permissions()).hasSize(1);
        assertThat(response.permissions().get(0).resourceName()).isEqualTo(ResourceType.FACILITY.getResourceName());
        assertThat(response.permissions().get(0).resourceIds())
                .containsExactlyInAnyOrder(buildings.get(0).getId(), buildings.get(1).getId());
    }

    @Test
    @DisplayName("ID로 Role 조회 시, 그룹화된 권한 정보까지 포함하여 반환한다")
    void findById_returnsRoleWithGroupedPermissions() {
        // GIVEN
        Long roleId = roleService.save(createRequest);
        em.flush();
        em.clear();

        // WHEN
        RoleResponse response = roleService.findById(roleId);

        // THEN
        assertThat(response.name()).isEqualTo("Test Role");
        assertThat(response.permissions()).isNotNull();
        assertThat(response.permissions()).hasSize(1);
        assertThat(response.permissions().get(0).resourceIds()).hasSize(2);
        assertThat(response.permissions().get(0).resourceIds())
                .containsExactlyInAnyOrder(buildings.get(0).getId(), buildings.get(1).getId());
    }

    @Test
    @DisplayName("Role 업데이트 후, 서비스를 통해 조회하여 변경사항과 권한 동기화를 검증한다")
    void update_andVerifyWithService() {
        // GIVEN: 1, 2번 건물 권한을 가진 Role을 먼저 생성
        Long roleId = roleService.save(createRequest);
        em.flush();
        em.clear();

        // 업데이트 요청: 1번은 삭제, 2번은 유지, 3번은 새로 추가 -> 최종 권한은 2, 3번 건물
        List<Long> updatedPermissionIds = List.of(buildings.get(1).getId(), buildings.get(2).getId());
        PermissionRequest updatedPermissionRequest = new PermissionRequest(ResourceType.FACILITY, updatedPermissionIds);
        RoleUpdateRequest updateRequest = new RoleUpdateRequest("Updated Role", "Updated Description", List.of(updatedPermissionRequest));

        // WHEN
        roleService.update(roleId, updateRequest);
        em.flush();
        em.clear();

        // THEN: Repository 대신 Service를 통해 조회하여 검증합니다.
        RoleResponse response = roleService.findById(roleId);

        assertThat(response.name()).isEqualTo("Updated Role");
        assertThat(response.description()).isEqualTo("Updated Description");

        // 최종 권한이 올바르게 동기화되었는지 검증
        assertThat(response.permissions()).hasSize(1);
        assertThat(response.permissions().get(0).resourceIds()).hasSize(2);
        assertThat(response.permissions().get(0).resourceIds()).containsExactlyInAnyOrderElementsOf(updatedPermissionIds);
    }

    @Test
    @DisplayName("Role 삭제 후, 서비스를 통해 조회 시 예외가 발생하는지 검증한다")
    void delete_andVerifyDeletionWithService() {
        // GIVEN
        Long roleId = roleService.save(createRequest);

        // 삭제 전에는 조회가 가능해야 함
        assertThat(roleService.findById(roleId)).isNotNull();

        // Permission 엔티티 자체는 삭제되지 않는 것을 확인하기 위해 미리 조회
        long initialPermissionCount = permissionRepository.count();

        // WHEN
        roleService.delete(roleId);
        em.flush();
        em.clear();

        // THEN
        // 1. 삭제된 Role ID로 조회 시도 시 EntityNotFoundException이 발생해야 함
        assertThrows(EntityNotFoundException.class, () -> roleService.findById(roleId));

        // 2. [중요] Permission 엔티티 자체는 삭제되지 않고 그대로 남아있어야 함을 검증
        // 이 부분은 RoleService의 책임 범위를 벗어나는 사이드 이펙트 검증이므로,
        // 예외적으로 PermissionRepository를 직접 사용하여 확인합니다.
        assertThat(permissionRepository.count()).isEqualTo(initialPermissionCount);
    }

    @Test
    @DisplayName("존재하지 않는 Role ID로 조회 시 예외가 발생한다")
    void findById_withNonExistentId_throwsException() {
        // GIVEN
        Long nonExistentId = 9999L;

        // WHEN & THEN
        assertThrows(EntityNotFoundException.class, () -> roleService.findById(nonExistentId));
    }
}