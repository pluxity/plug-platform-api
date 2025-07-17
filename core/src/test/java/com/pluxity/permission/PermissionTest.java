package com.pluxity.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.building.Building;
import com.pluxity.building.BuildingRepository;
import com.pluxity.building.BuildingService;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityRepository;
import com.pluxity.facility.FacilityService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.PermissionRequest;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import com.pluxity.user.service.PermissionService;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class PermissionTest {

    @Autowired private PermissionService permissionService;
    @Autowired private FacilityService facilityService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private FacilityRepository facilityRepository;
    @Autowired private EntityManager em;

    private User adminUser;
    private User editorUser;
    private Role editorRole;
    private final List<Building> buildings = new ArrayList<>();
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private BuildingRepository buildingRepository;

    @BeforeEach
    void setUp() {
        // 1. 역할 생성
        Role adminRole = roleRepository.save(Role.builder().name("SUPER_ADMIN").build());
        editorRole = roleRepository.save(Role.builder().name("EDITOR").build());

        // 2. 사용자 생성 및 역할 부여
        adminUser = User.builder().username("admin").password("pw").name("관리자").build();
        adminUser.addRole(adminRole);
        userRepository.save(adminUser);

        editorUser = User.builder().username("editor").password("pw").name("편집자").build();
        editorUser.addRole(editorRole);
        userRepository.save(editorUser);

        // 3. 테스트 데이터(Facility) 생성
        IntStream.rangeClosed(1, 10).forEach(i -> buildings.add(buildingRepository.save(new Building("Building " + i, "B" + i, null, null, null))));

        // SecurityContextHolder 클리어
        SecurityContextHolder.clearContext();
        em.flush();
        em.clear();
    }

    private void setAuthentication(User user) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user.getUsername(), null, null)); // 간단한 인증 객체
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("권한 동기화 후, 해당 권한으로 목록 조회 및 단건 조회가 올바르게 동작하는지 테스트")
    void syncAndCheckPermissions() {
        // === Act 1: facility 1, 3, 5번 권한 부여 ===
        setAuthentication(adminUser); // 관리자 권한으로 실행
        List<Long> initialPermissionIds = List.of(buildings.get(0).getId(), buildings.get(2).getId(), buildings.get(4).getId());
        permissionService.syncPermissions(new PermissionRequest(editorRole.getId(), ResourceType.FACILITY, initialPermissionIds));

        // 영속성 컨텍스트를 flush하여 변경사항을 DB에 반영하고, clear하여 캐시를 비움
        // 이후 조회 시 DB에서 직접 가져오도록 함
        em.flush();
        em.clear();

        // === Assert 1: editorUser 권한으로 검증 ===
        setAuthentication(editorUser); // 편집자 권한으로 변경

        // findAll() 결과 검증
        List<Facility> accessibleFacilities = facilityService.findAll();
        assertThat(accessibleFacilities).hasSize(3);
        assertThat(accessibleFacilities.stream().map(Facility::getId).toList()).containsExactlyInAnyOrderElementsOf(initialPermissionIds);

        // findById() 성공/실패 검증
        assertThat(facilityService.findById(buildings.get(0).getId())).isNotNull();
        assertThrows(CustomException.class, () -> facilityService.findById(buildings.get(1).getId()));


        // === Act 2: Facility 5, 7, 9번으로 권한 변경 ===
        setAuthentication(adminUser); // 다시 관리자 권한으로
        List<Long> updatedPermissionIds = List.of(buildings.get(4).getId(), buildings.get(6).getId(), buildings.get(8).getId());
        permissionService.syncPermissions(new PermissionRequest(editorRole.getId(), ResourceType.FACILITY, updatedPermissionIds));

        em.flush();
        em.clear();

        // === Assert 2: 변경된 권한으로 editorUser 검증 ===
        setAuthentication(editorUser); // 다시 편집자 권한으로

        // findAll() 결과 재검증
        List<Facility> updatedAccessibleFacilities = facilityService.findAll();
        assertThat(updatedAccessibleFacilities).hasSize(3);
        assertThat(updatedAccessibleFacilities.stream().map(Facility::getId).toList()).containsExactlyInAnyOrderElementsOf(updatedPermissionIds);

        // findById() 성공/실패 재검증
        assertThrows(CustomException.class, () -> {
            facilityService.findById(buildings.getFirst().getId()); // 이전 권한은 이제 접근 불가
        });
        assertThat(facilityService.findById(buildings.get(6).getId())).isNotNull(); // 새 권한은 접근 가능
    }
}
