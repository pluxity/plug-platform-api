package com.pluxity.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.building.Building;
import com.pluxity.building.BuildingRepository;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.PermissionCreateRequest;
import com.pluxity.user.dto.RoleCreateRequest;
import com.pluxity.user.dto.UserRoleAssignRequest;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import com.pluxity.user.service.PermissionService;
import com.pluxity.user.service.RoleService;
import com.pluxity.user.service.UserService;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserRolePermissionIntegrationTest {

  @Autowired private UserService userService;
  @Autowired private RoleService roleService;
  @Autowired private PermissionService permissionService; // PermissionService 주입
  @Autowired private FacilityService facilityService; // 권한 테스트 대상 서비스
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private PermissionRepository permissionRepository; // PermissionRepository 주입
  @Autowired private BuildingRepository buildingRepository;
  @Autowired private EntityManager em;

  private User adminUser;
  private User editorUser;
  private Role adminRole;
  private final List<Building> buildings = new ArrayList<>();


  @BeforeEach
  void setUp() {
    // 1. 기본 역할 생성
    adminRole = roleRepository.save(Role.builder().name("ADMIN").description("관리자").build());

    // 2. 기본 사용자 생성
    adminUser =
            userRepository.save(User.builder().username("admin").password("pw").name("관리자").build());
    adminUser.addRole(adminRole);

    editorUser =
            userRepository.save(User.builder().username("editor").password("pw").name("편집자").build());

    // 3. 테스트용 리소스(Facility) 5개 생성
    buildings.clear();

    IntStream.rangeClosed(1, 5)
            .forEach(
                    i -> {
                      // Facility는 Building을 상속받지 않으므로 Building 생성 로직 제거
                      buildings.add(buildingRepository.save(Building.builder().name("Facility " + i).code("F" + i).build()));
                    });

    em.flush();
    em.clear();
  }

  private void setAuthentication(User user) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(
            new UsernamePasswordAuthenticationToken(user.getUsername(), null, null));
    SecurityContextHolder.setContext(context);
  }

  @Test
  @DisplayName("특정 시설 접근 권한을 가진 역할을 부여받은 사용자는, 허가된 시설만 조회할 수 있다")
  void userWithSpecificRole_canOnlyAccessPermittedResources() {
    // === GIVEN: '편집자' 사용자에게 1번, 3번 시설에 대한 접근 권한만 부여 ===

    // 1. 관리자로 로그인하여 역할을 생성하고 사용자에게 할당합니다.
    setAuthentication(adminUser);

    // 2. [수정] 1번, 3번 시설에 대한 Permission을 미리 생성합니다.
    PermissionCreateRequest createPerm1Request = new PermissionCreateRequest(ResourceType.FACILITY, String.valueOf(buildings.get(0).getId()));
    PermissionCreateRequest createPerm3Request = new PermissionCreateRequest(ResourceType.FACILITY, String.valueOf(buildings.get(2).getId()));

    Long permission1Id = permissionService.create(createPerm1Request);
    Long permission3Id = permissionService.create(createPerm3Request);

    List<Long> permittedPermissionIds = List.of(permission1Id, permission3Id);

    // 3. [수정] "시설 관리자" 역할을 생성하면서 위에서 생성한 Permission들의 ID 목록을 전달합니다.
    RoleCreateRequest createRoleRequest =
            new RoleCreateRequest("시설 관리자", "1, 3번 시설 접근 가능", permittedPermissionIds);
    Long newRoleId = roleService.save(createRoleRequest);

    // 4. 생성된 "시설 관리자" 역할을 '편집자' 사용자에게 할당합니다.
    userService.assignRolesToUser(
            editorUser.getId(), new UserRoleAssignRequest(List.of(newRoleId)));

    em.flush();
    em.clear();

    // === WHEN: '편집자' 사용자로 로그인하여 시설 목록을 조회 ===

    // 5. 이제 '편집자'로 로그인한 상황을 시뮬레이션합니다.
    setAuthentication(editorUser);

    // 6. 전체 시설 목록을 조회합니다. (AOP가 이 호출을 가로채 결과를 필터링할 것을 기대)
    List<Facility> accessibleFacilities = facilityService.findAll();

    // === THEN: 오직 허가된 시설만 조회되어야 함 ===

    // 7. 조회된 시설은 정확히 2개여야 합니다.
    assertThat(accessibleFacilities).hasSize(2);

    // 8. 조회된 시설 목록의 ID가 우리가 허가한 시설의 ID와 일치하는지 확인합니다.
    List<Long> permittedFacilityIds = List.of(buildings.get(0).getId(), buildings.get(2).getId());
    List<Long> accessibleIds = accessibleFacilities.stream()
            .map(Facility::getId)
            .collect(Collectors.toList());
    assertThat(accessibleIds).containsExactlyInAnyOrderElementsOf(permittedFacilityIds);

    // 9. 추가 검증: 허가된 시설(1번)에 ID로 직접 접근하면 성공해야 합니다.
    Long permittedId = buildings.get(0).getId();
    assertDoesNotThrow(() -> facilityService.findById(permittedId));

    // 10. 추가 검증: 허가되지 않은 시설(2번)에 ID로 직접 접근하면 예외가 발생해야 합니다.
    Long forbiddenId = buildings.get(1).getId();
    assertThrows(
            CustomException.class,
            () -> facilityService.findById(forbiddenId),
            "허가되지 않은 리소스 접근 시 CustomException이 발생해야 합니다.");
  }
}