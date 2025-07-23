package com.pluxity.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.building.Building;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityRepository;
import com.pluxity.facility.FacilityService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.PermissionRequest;
import com.pluxity.user.dto.RoleCreateRequest;
import com.pluxity.user.dto.UserRoleAssignRequest;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
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
  @Autowired private FacilityService facilityService; // 권한 테스트 대상 서비스
  @Autowired private UserRepository userRepository;
  @Autowired private RoleRepository roleRepository;
  @Autowired private FacilityRepository facilityRepository;
  @Autowired private EntityManager em;

  private User adminUser;
  private User editorUser;
  private Role adminRole;
  private final List<Facility> facilities = new ArrayList<>();

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
    facilities.clear();

    IntStream.rangeClosed(1, 5)
        .forEach(
            i -> {
              Building building = Building.builder().name("Facility" + i).code("F" + i).build();
              facilities.add(facilityRepository.save(building));
            });

    em.flush();
    em.clear();
  }

  private void setAuthentication(User user) {
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    // 실제 인증 절차를 밟는 것이 아니므로, 간단한 인증 토큰을 생성하여 SecurityContext에 설정합니다.
    // AOP Aspect에서는 이 컨텍스트에서 사용자 이름을 꺼내 권한을 검사하게 됩니다.
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

    // 2. 1번, 3번 시설에 대한 접근 권한을 정의합니다.
    List<Long> permittedFacilityIds = List.of(facilities.get(0).getId(), facilities.get(2).getId());
    PermissionRequest permissionRequest =
        new PermissionRequest(ResourceType.FACILITY, permittedFacilityIds);

    // 3. "시설 관리자" 역할을 생성하면서 위에서 정의한 권한을 부여합니다.
    RoleCreateRequest createRoleRequest =
        new RoleCreateRequest("시설 관리자", "1, 3번 시설 접근 가능", List.of(permissionRequest));
    Long newRoleId = roleService.save(createRoleRequest);

    // 4. 생성된 "시설 관리자" 역할을 '편집자' 사용자에게 할당합니다.
    userService.assignRolesToUser(
        editorUser.getId(), new UserRoleAssignRequest(List.of(newRoleId)));

    // 영속성 컨텍스트를 초기화하여 이후 작업이 DB에서 직접 읽도록 보장합니다.
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

    // 8. 조회된 시설 목록의 ID가 우리가 허가한 ID 목록과 일치하는지 확인합니다.
    List<Long> accessibleIds =
        accessibleFacilities.stream().map(Facility::getId).collect(Collectors.toList());
    assertThat(accessibleIds).containsExactlyInAnyOrderElementsOf(permittedFacilityIds);

    // 9. 추가 검증: 허가된 시설(1번)에 ID로 직접 접근하면 성공해야 합니다.
    Long permittedId = facilities.get(0).getId();
    assertDoesNotThrow(() -> facilityService.findById(permittedId));

    // 10. 추가 검증: 허가되지 않은 시설(2번)에 ID로 직접 접근하면 예외가 발생해야 합니다.
    Long forbiddenId = facilities.get(1).getId();
    assertThrows(
        CustomException.class,
        () -> facilityService.findById(forbiddenId),
        "허가되지 않은 리소스 접근 시 CustomException이 발생해야 합니다.");
  }
}
