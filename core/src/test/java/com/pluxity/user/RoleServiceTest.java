package com.pluxity.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.building.Building;
import com.pluxity.building.BuildingRepository;
import com.pluxity.permission.PermissionGroupRepository;
import com.pluxity.permission.PermissionGroupService;
import com.pluxity.permission.PermissionRepository;
import com.pluxity.permission.ResourceType;
import com.pluxity.permission.dto.PermissionGroupCreateRequest;
import com.pluxity.permission.dto.PermissionRequest;
import com.pluxity.permission.dto.PermissionResponse;
import com.pluxity.user.dto.*;
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
    @Autowired private PermissionGroupService permissionGroupService; // PermissionService -> PermissionGroupService
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private PermissionGroupRepository permissionGroupRepository; // 추가
    @Autowired private BuildingRepository buildingRepository;
    @Autowired private EntityManager em;

    private final List<Building> buildings = new ArrayList<>();
    // permissionIds -> permissionGroupIds
    private final List<Long> permissionGroupIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 건물(리소스) 생성
        buildings.clear();
        IntStream.rangeClosed(1, 3).forEach(i ->
                buildings.add(buildingRepository.save(Building.builder().name("Building " + i).code("B" + i).build()))
        );

        // [수정] 테스트에 사용할 권한 그룹(PermissionGroup)을 미리 생성
        permissionGroupIds.clear();
    buildings.forEach(
        building -> {
          // 각 건물 ID에 대해 하나의 권한을 가진 그룹을 생성
          PermissionGroupCreateRequest request =
              new PermissionGroupCreateRequest(
                  "Building " + building.getId() + " Group",
                  "Description for " + building.getName(),
                  List.of(
                      new PermissionRequest(
                          ResourceType.FACILITY.name(),
                          List.of(String.valueOf(building.getId())))));
          Long groupId = permissionGroupService.create(request);
          permissionGroupIds.add(groupId);
        });

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("새로운 Role을 권한 그룹과 함께 생성하고, 생성된 Role을 서비스로 조회하여 검증한다")
    void save_withPermissionGroups_andVerifyWithService() {
        // GIVEN
        // 1번, 2번 건물에 대한 권한 그룹 ID만 사용하여 Role 생성
        List<Long> initialGroupIds = List.of(permissionGroupIds.get(0), permissionGroupIds.get(1));
        RoleCreateRequest createRequest = new RoleCreateRequest("Test Role", "A role for testing", initialGroupIds);

        // WHEN
        Long roleId = roleService.save(createRequest);
        em.flush();
        em.clear();

        // THEN: Service를 통해 조회하여 검증
        RoleResponse response = roleService.findById(roleId);

        assertThat(response.name()).isEqualTo("Test Role");
        assertThat(response.description()).isEqualTo("A role for testing");

        // Role이 가진 PermissionGroup 목록을 검증
        // RoleResponse가 PermissionGroup ID 목록을 직접 반환한다고 가정
        // (만약 아니라면, Role 엔티티를 직접 조회해서 확인해야 함)
        List<Long> responseGroupIds = roleService.findRoleById(roleId).getRolePermissions().stream()
                .map(rp -> rp.getPermissionGroup().getId())
                .collect(Collectors.toList());

        assertThat(responseGroupIds).hasSize(2);
        assertThat(responseGroupIds).containsExactlyInAnyOrderElementsOf(initialGroupIds);
    }

    @Test
    @DisplayName("ID로 Role 조회 시, 할당된 모든 권한 그룹의 상세 권한 정보까지 포함하여 반환한다")
    void findById_returnsRoleWithAllPermissionsInGroups() {
        // GIVEN
        List<Long> initialGroupIds = List.of(permissionGroupIds.get(0), permissionGroupIds.get(1));
        RoleCreateRequest createRequest = new RoleCreateRequest("Test Role", "For findById test", initialGroupIds);
        Long roleId = roleService.save(createRequest);
        em.flush();
        em.clear();

        // WHEN
        RoleResponse response = roleService.findById(roleId);

        // THEN
        assertThat(response.name()).isEqualTo("Test Role");
        assertThat(response.permissions()).isNotNull();
        // 각 그룹에 Permission이 1개씩 있으므로, 총 2개의 Permission이 조회되어야 함
        assertThat(response.permissions()).hasSize(2);

        List<String> responseResourceIds = response.permissions().stream()
                .map(PermissionResponse::resourceId)
                .collect(Collectors.toList());

        assertThat(responseResourceIds).containsExactlyInAnyOrder(
                String.valueOf(buildings.get(0).getId()),
                String.valueOf(buildings.get(1).getId())
        );
    }

    @Test
    @DisplayName("Role 업데이트 후, 서비스를 통해 조회하여 변경사항과 권한 그룹 동기화를 검증한다")
    void update_andVerifyWithService() {
        // GIVEN: 1, 2번 건물 권한 그룹을 가진 Role을 먼저 생성
        Long roleId = roleService.save(new RoleCreateRequest("Initial Role", "Desc", List.of(permissionGroupIds.get(0), permissionGroupIds.get(1))));
        em.flush();
        em.clear();

        // 업데이트 요청: 1번은 삭제, 2번은 유지, 3번은 새로 추가 -> 최종 권한 그룹은 2, 3번
        List<Long> updatedGroupIdList = List.of(permissionGroupIds.get(1), permissionGroupIds.get(2));
        RoleUpdateRequest updateRequest = new RoleUpdateRequest("Updated Role", "Updated Description", updatedGroupIdList);

        // WHEN
        roleService.update(roleId, updateRequest);
        em.flush();
        em.clear();

        // THEN: Service를 통해 조회하여 검증
        RoleResponse response = roleService.findById(roleId);

        assertThat(response.name()).isEqualTo("Updated Role");
        assertThat(response.description()).isEqualTo("Updated Description");

        // 최종 권한이 올바르게 동기화되었는지 검증 (Permission 2개 확인)
        assertThat(response.permissions()).hasSize(2);
        List<String> finalResourceIds = response.permissions().stream().map(PermissionResponse::resourceId).collect(Collectors.toList());
        assertThat(finalResourceIds).containsExactlyInAnyOrder(
                String.valueOf(buildings.get(1).getId()),
                String.valueOf(buildings.get(2).getId())
        );
    }

    @Test
    @DisplayName("Role 삭제 후, 서비스를 통해 조회 시 예외가 발생하는지 검증한다")
    void delete_andVerifyDeletionWithService() {
        // GIVEN
        Long roleId = roleService.save(new RoleCreateRequest("Deletable Role", "Desc", List.of(permissionGroupIds.get(0))));
        assertThat(roleService.findById(roleId)).isNotNull();

        long initialGroupCount = permissionGroupRepository.count();

        // WHEN
        roleService.delete(roleId);
        em.flush();
        em.clear();

        // THEN
        assertThrows(EntityNotFoundException.class, () -> roleService.findById(roleId));

        // [중요] PermissionGroup 엔티티 자체는 삭제되지 않고 그대로 남아있어야 함을 검증
        assertThat(permissionGroupRepository.count()).isEqualTo(initialGroupCount);
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