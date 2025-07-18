package com.pluxity.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.building.Building;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityRepository;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.category.FacilityCategory;
import com.pluxity.facility.category.FacilityCategoryRepository;
import com.pluxity.facility.category.FacilityCategoryService;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.PermissionRequest;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import com.pluxity.user.service.PermissionService;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class FacilityPermissionTest {

    @Autowired private PermissionService permissionService;
    @Autowired private FacilityService facilityService;
    @Autowired private FacilityCategoryService facilityCategoryService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FacilityRepository facilityRepository;
    @Autowired private FacilityCategoryRepository facilityCategoryRepository;
    @Autowired private EntityManager em;

    @MockBean // FileService는 외부 의존성이므로 Mock으로 대체
    private FileService fileService;

    private User adminUser;
    private User editorUser;
    private Role editorRole;

    private FacilityCategory categoryHq, categoryBuildingA, categoryBuildingB;
    private FacilityCategory categoryA_1F, categoryA_2F, categoryB_1F;
    private Facility facilityA1, facilityA2, facilityB1;

    @BeforeEach
    void setUp() {
        // 1. 역할 및 사용자 생성
        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        editorRole = roleRepository.save(Role.builder().name("EDITOR").build());

        adminUser = userRepository.save(User.builder().username("admin").password("pw").name("관리자").build());
        adminUser.addRole(adminRole);
        editorUser = userRepository.save(User.builder().username("editor").password("pw").name("편집자").build());
        editorUser.addRole(editorRole);

        categoryHq = facilityCategoryRepository.save(FacilityCategory.builder().name("본사").parent(null).build());
        categoryBuildingA = facilityCategoryRepository.save(FacilityCategory.builder().name("A 빌딩").parent(categoryHq).build());
        categoryBuildingB = facilityCategoryRepository.save(FacilityCategory.builder().name("B 빌딩").parent(categoryHq).build());
        categoryA_1F = facilityCategoryRepository.save(FacilityCategory.builder().name("A 빌딩 1층").parent(categoryBuildingA).build());
        categoryA_2F = facilityCategoryRepository.save(FacilityCategory.builder().name("A 빌딩 2층").parent(categoryBuildingA).build());
        categoryB_1F = facilityCategoryRepository.save(FacilityCategory.builder().name("B 빌딩 1층").parent(categoryBuildingB).build());

        facilityA1 = facilityRepository.save(new Building("A-101", "A101", null, null, null));
        facilityA1.assignCategory(categoryA_1F);
        facilityA2 = facilityRepository.save(new Building("A-201", "A201", null, null, null));
        facilityA2.assignCategory(categoryA_2F);
        facilityB1 = facilityRepository.save(new Building("B-101", "B101", null, null, null));
        facilityB1.assignCategory(categoryB_1F);

        em.flush();
        em.clear();
    }

    private void setAuthentication(User user) {
        // ... 기존 setAuthentication() 내용은 그대로 ...
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getUsername(), null, null));
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("특정 카테고리들에 대한 권한만 부여했을 때, findAll 호출 시 정확히 해당 카테고리들만 반환되어야 한다")
    void givenDirectPermissions_whenFindAll_shouldReturnOnlyPermittedNodes() {
        // === GIVEN: 'A 빌딩 1층'과 'B 빌딩' 두 곳에만 직접 권한 부여 ===
        setAuthentication(adminUser);
        permissionService.syncPermissions(new PermissionRequest(editorRole.getId(), ResourceType.FACILITY_CATEGORY,
                List.of(categoryA_1F.getId(), categoryBuildingB.getId())));

        em.flush();
        em.clear();

        // === WHEN: EDITOR 권한으로 조회 ===
        setAuthentication(editorUser);
        // 서비스는 모든 데이터를 조회하지만, AOP가 필터링한 최종 리스트를 반환함
        List<FacilityCategory> accessibleCategories = facilityCategoryService.findAllTest();

        // === THEN: 최종 목록 검증 ===
        // AOP 필터링 후, 목록에는 정확히 권한을 부여한 2개의 카테고리만 있어야 함
        assertThat(accessibleCategories).hasSize(2);

        // 반환된 목록의 ID가 'A 빌딩 1층'과 'B 빌딩'의 ID와 일치하는지 확인
        assertThat(accessibleCategories.stream().map(FacilityCategory::getId))
                .containsExactlyInAnyOrder(categoryA_1F.getId(), categoryBuildingB.getId());

        // 상위 카테고리인 'A 빌딩'이나 '본사'는 포함되어서는 안 됨
        assertThat(accessibleCategories.stream().map(FacilityCategory::getId))
                .doesNotContain(categoryBuildingA.getId(), categoryHq.getId());
    }

    @Test
    @DisplayName("시설 조회 시, 해당 시설의 카테고리에 직접 권한이 있을 때만 조회가 성공해야 한다")
    void givenDirectPermission_whenFindById_shouldSucceed() {
        // === GIVEN: 'A 빌딩 1층' 카테고리에만 권한 부여 ===
        setAuthentication(adminUser);
        permissionService.syncPermissions(new PermissionRequest(editorRole.getId(), ResourceType.FACILITY_CATEGORY,
                List.of(categoryA_1F.getId())));
        em.flush();
        em.clear();

        // === WHEN & THEN: EDITOR 권한으로 조회 ===
        setAuthentication(editorUser);

        // 'A 빌딩 1층'에 속한 facilityA1은 조회가 성공해야 함
        assertNotNull(facilityService.findById(facilityA1.getId()));

        // 'B 빌딩'에 속한 facilityB1은 조회가 실패해야 함
        assertThrows(CustomException.class, () -> {
            facilityService.findById(facilityB1.getId());
        });
    }

    @Test
    @DisplayName("접근 가능한 카테고리 목록 조회 시, 해당 카테고리들에 속한 시설 목록만 정확히 반환되어야 한다")
    void givenCategoryPermissions_whenGetHierarchicalCategories_shouldReturnOnlyFacilitiesInPermittedCategories() {
        // === GIVEN: 'A 빌딩 1층'과 'B 빌딩' 두 곳에만 직접 권한 부여 ===
        // 'A 빌딩 1층'에는 facilityA1이 있고, 'B 빌딩'에는 직접 속한 시설이 없음
        setAuthentication(adminUser);
        permissionService.syncPermissions(new PermissionRequest(editorRole.getId(), ResourceType.FACILITY_CATEGORY,
                List.of(categoryA_1F.getId(), categoryBuildingB.getId())));

        em.flush();
        em.clear();

        // === WHEN: EDITOR 권한으로 getHierarchicalCategories 호출 ===
        setAuthentication(editorUser);
        List<FacilityCategoryService.FacilityResponse> accessibleFacilities = facilityCategoryService.getFacilityWithGrouping();

        // === THEN: 최종 시설 목록 검증 ===
        // 'A 빌딩 1층'에 직접 속한 facilityA1만 반환되어야 함
        // 'B 빌딩'은 권한이 있지만 직접 속한 facility가 없으므로 아무것도 반환되지 않음
        assertThat(accessibleFacilities).hasSize(1);

        // 반환된 유일한 시설이 facilityA1과 일치하는지 상세히 확인
        FacilityCategoryService.FacilityResponse response = accessibleFacilities.get(0);
        assertThat(response.getId()).isEqualTo(facilityA1.getId());
        assertThat(response.getName()).isEqualTo(facilityA1.getName());
        assertThat(response.getCode()).isEqualTo(facilityA1.getCode());
        assertThat(response.getCategoryName()).isEqualTo(categoryA_1F.getName());

        // 다른 시설들(facilityA2, facilityB1)은 포함되지 않았는지 확인
        assertThat(accessibleFacilities.stream().map(FacilityCategoryService.FacilityResponse::getId))
                .doesNotContain(facilityA2.getId(), facilityB1.getId());
    }
}