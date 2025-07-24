package com.pluxity.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.building.Building;
import com.pluxity.building.BuildingRepository;
import com.pluxity.user.dto.*;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.service.PermissionService;
import com.pluxity.user.service.RoleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
@ActiveProfiles("test")
class RoleServiceTest {
    @Autowired private RoleService roleService;
    @Autowired private PermissionService permissionService;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private EntityManager em;

    private final List<Building> buildings = new ArrayList<>();
    private final List<Long> permissionIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 건물(리소스) 생성
        buildings.clear();
        IntStream.rangeClosed(1, 3).forEach(i ->
                buildings.add(buildingRepository.save(Building.builder().name("Building " + i).code("B" + i).build()))
        );

        // 테스트에 사용할 권한(Permission) 미리 생성
        permissionIds.clear();
        buildings.forEach(building -> {
            PermissionCreateRequest request = new PermissionCreateRequest(ResourceType.FACILITY, String.valueOf(building.getId()));
            Long permissionId = permissionService.create(request);
            permissionIds.add(permissionId);
        });

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("새로운 Role을 권한과 함께 생성하고, 생성된 Role을 서비스로 조회하여 검증한다")
    void save_withPermissions_andVerifyWithService() {
        // GIVEN
        // 1번, 2번 건물에 대한 권한 ID만 사용하여 Role 생성
        List<Long> initialPermissionIds = List.of(permissionIds.get(0), permissionIds.get(1));
        RoleCreateRequest createRequest = new RoleCreateRequest("Test Role", "A role for testing", initialPermissionIds);

        // WHEN
        Long roleId = roleService.save(createRequest);
        em.flush();
        em.clear();

        // THEN: Service를 통해 조회하여 검증
        RoleResponse response = roleService.findById(roleId);

        assertThat(response.name()).isEqualTo("Test Role");
        assertThat(response.description()).isEqualTo("A role for testing");

        // [수정] Role이 개별 Permission 목록을 올바르게 포함하는지 검증
        assertThat(response.permissions()).hasSize(2);

        List<Long> responsePermissionIds = response.permissions().stream()
                .map(PermissionResponse::id)
                .collect(Collectors.toList());
        assertThat(responsePermissionIds).containsExactlyInAnyOrderElementsOf(initialPermissionIds);
    }

    @Test
    @DisplayName("ID로 Role 조회 시, 할당된 모든 권한 정보까지 포함하여 반환한다")
    void findById_returnsRoleWithAllPermissions() {
        // GIVEN
        List<Long> initialPermissionIds = List.of(permissionIds.get(0), permissionIds.get(1));
        RoleCreateRequest createRequest = new RoleCreateRequest("Test Role", "For findById test", initialPermissionIds);
        Long roleId = roleService.save(createRequest);
        em.flush();
        em.clear();

        // WHEN
        RoleResponse response = roleService.findById(roleId);

        // THEN
        assertThat(response.name()).isEqualTo("Test Role");
        assertThat(response.permissions()).isNotNull();
        assertThat(response.permissions()).hasSize(2);

        // 응답에 포함된 Permission의 resourceId가 실제 건물의 ID와 일치하는지 검증
        List<String> responseResourceIds = response.permissions().stream()
                .map(PermissionResponse::resourceId)
                .collect(Collectors.toList());

        assertThat(responseResourceIds).containsExactlyInAnyOrder(
                String.valueOf(buildings.get(0).getId()),
                String.valueOf(buildings.get(1).getId())
        );
    }

    @Test
    @DisplayName("Role 업데이트 후, 서비스를 통해 조회하여 변경사항과 권한 동기화를 검증한다")
    void update_andVerifyWithService() {
        // GIVEN: 1, 2번 건물 권한을 가진 Role을 먼저 생성
        Long roleId = roleService.save(new RoleCreateRequest("Initial Role", "Desc", List.of(permissionIds.get(0), permissionIds.get(1))));
        em.flush();
        em.clear();

        // 업데이트 요청: 1번은 삭제, 2번은 유지, 3번은 새로 추가 -> 최종 권한은 2, 3번 건물
        List<Long> updatedPermissionIdList = List.of(permissionIds.get(1), permissionIds.get(2));
        RoleUpdateRequest updateRequest = new RoleUpdateRequest("Updated Role", "Updated Description", updatedPermissionIdList);

        // WHEN
        roleService.update(roleId, updateRequest);
        em.flush();
        em.clear();

        // THEN: Service를 통해 조회하여 검증
        RoleResponse response = roleService.findById(roleId);

        assertThat(response.name()).isEqualTo("Updated Role");
        assertThat(response.description()).isEqualTo("Updated Description");

        // 최종 권한이 올바르게 동기화되었는지 검증
        assertThat(response.permissions()).hasSize(2);
        List<Long> finalPermissionIds = response.permissions().stream().map(PermissionResponse::id).collect(Collectors.toList());
        assertThat(finalPermissionIds).containsExactlyInAnyOrderElementsOf(updatedPermissionIdList);
    }

    @Test
    @DisplayName("Role 삭제 후, 서비스를 통해 조회 시 예외가 발생하는지 검증한다")
    void delete_andVerifyDeletionWithService() {
        // GIVEN
        Long roleId = roleService.save(new RoleCreateRequest("Deletable Role", "Desc", List.of(permissionIds.get(0))));
        assertThat(roleService.findById(roleId)).isNotNull(); // 삭제 전에는 조회가 가능해야 함

        // Permission 엔티티 자체는 삭제되지 않는 것을 확인하기 위해 미리 개수 조회
        long initialPermissionCount = permissionRepository.count();

        // WHEN
        roleService.delete(roleId);
        em.flush();
        em.clear();

        // THEN
        // 1. 삭제된 Role ID로 조회 시도 시 EntityNotFoundException이 발생해야 함
        assertThrows(EntityNotFoundException.class, () -> roleService.findById(roleId));

        // 2. [중요] Permission 엔티티 자체는 삭제되지 않고 그대로 남아있어야 함을 검증
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