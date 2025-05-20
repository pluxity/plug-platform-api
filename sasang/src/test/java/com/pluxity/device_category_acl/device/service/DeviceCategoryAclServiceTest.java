package com.pluxity.device_category_acl.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.domains.acl.service.AclManagerService;
import com.pluxity.domains.device_category_acl.device.dto.DeviceCategoryResponseDto;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto.PermissionOperation;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto.PermissionTarget;
import com.pluxity.domains.device_category_acl.device.service.DeviceCategoryAclService;
import com.pluxity.user.dto.*;
import com.pluxity.user.service.RoleService;
import com.pluxity.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = SasangApplication.class)
@Transactional
@ActiveProfiles("test")
class DeviceCategoryAclServiceTest {

    @Autowired
    private DeviceCategoryAclService deviceCategoryAclService;

    @Autowired
    private AclManagerService aclManagerService;
    
    @Autowired
    private MutableAclService mutableAclService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private DeviceCategoryRepository deviceCategoryRepository;

    private Authentication originalAuth;
    
    // 다수의 사용자, 역할, 카테고리 테스트를 위한 필드
    private final Map<String, DeviceCategory> deviceCategories = new HashMap<>();
    private final Map<String, Long> userIds = new HashMap<>();
    private final Map<String, String> roleAuthorities = new HashMap<>();
    private final Map<String, Long> roleIds = new HashMap<>();

    @BeforeEach
    void setUp() {
        originalAuth = SecurityContextHolder.getContext().getAuthentication();

        // 관리자 인증 설정
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
            "adminForTest", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        // 다양한 Device Category 생성
        createDeviceCategories();
        
        // 다양한 사용자 및 역할 생성
        createUsersAndRoles();
    }

    private void createDeviceCategories() {
        // 주요 카테고리 생성
        DeviceCategory mainCategory = DeviceCategory.builder()
            .name("메인 디바이스 카테고리")
            .parent(null)
            .build();
        deviceCategoryRepository.save(mainCategory);
        deviceCategories.put("main", mainCategory);
        
        // 서브 카테고리들 생성
        DeviceCategory subCategory1 = DeviceCategory.builder()
            .name("서브 카테고리 1")
            .parent(mainCategory)
            .build();
        deviceCategoryRepository.save(subCategory1);
        deviceCategories.put("sub1", subCategory1);
        
        DeviceCategory subCategory2 = DeviceCategory.builder()
            .name("서브 카테고리 2")
            .parent(mainCategory)
            .build();
        deviceCategoryRepository.save(subCategory2);
        deviceCategories.put("sub2", subCategory2);
        
        // 독립 카테고리 생성
        DeviceCategory independentCategory = DeviceCategory.builder()
            .name("독립 카테고리")
            .parent(null)
            .build();
        deviceCategoryRepository.save(independentCategory);
        deviceCategories.put("independent", independentCategory);
        
        // 중첩 서브 카테고리 생성
        DeviceCategory nestedCategory = DeviceCategory.builder()
            .name("중첩 서브 카테고리")
            .parent(subCategory1)
            .build();
        deviceCategoryRepository.save(nestedCategory);
        deviceCategories.put("nested", nestedCategory);
        
        // 각 카테고리에 대한 기존 ACL 삭제
        for (DeviceCategory category : deviceCategories.values()) {
            try {
                ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, category.getId());
                mutableAclService.deleteAcl(oi, false);
            } catch (NotFoundException e) {
                // ACL이 없으면 무시
            }
        }
    }

    private void createUsersAndRoles() {
        // 다양한 관리자 생성
        createUserWithRole("superAdmin", "슈퍼 관리자", "SUPER_ADMIN");
        createUserWithRole("systemAdmin", "시스템 관리자", "SYSTEM_ADMIN");
        
        // 다양한 일반 사용자 및 역할 생성
        createUserWithRole("editor", "편집자", "EDITOR");
        createUserWithRole("viewer", "뷰어", "VIEWER");
        createUserWithRole("manager", "매니저", "MANAGER");
        createUserWithRole("guest", "게스트", "GUEST");
    }
    
    private void createUserWithRole(String userKey, String userName, String roleName) {
        // 사용자 생성
        String username = userKey + System.currentTimeMillis();
        UserResponse userResponse = userService.save(new UserCreateRequest(
            username, "password123", userName, userKey.toUpperCase()
        ));
        userIds.put(userKey, userResponse.id());
        
        // 역할 생성
        String roleNameUnique = roleName + System.currentTimeMillis();
        RoleResponse roleResponse = roleService.save(new RoleCreateRequest(
            roleNameUnique, roleName + " 역할"
        ));
        roleIds.put(userKey, roleResponse.id());
        
        String roleAuthority = "ROLE_" + roleNameUnique;
        roleAuthorities.put(userKey, roleAuthority);
        
        // 사용자에게 역할 할당
        userService.assignRolesToUser(userIds.get(userKey), new UserRoleAssignRequest(List.of(roleIds.get(userKey))));
    }

    @AfterEach
    void tearDown() {
        // 모든 ACL 정리
        for (DeviceCategory category : deviceCategories.values()) {
            try {
                ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, category.getId());
                mutableAclService.deleteAcl(oi, false);
            } catch (NotFoundException e) {
                // ACL이 없으면 무시
            }
        }
        
        // 데이터베이스 정리
        deviceCategoryRepository.deleteAll();
        
        // 보안 컨텍스트 복원
        SecurityContextHolder.getContext().setAuthentication(originalAuth);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("ADMIN 역할로 DeviceCategory에 역할 권한 부여")
    void grantPermission_forRole_byAdmin() {
        // given
        DeviceCategory category = deviceCategories.get("main");
        String editorAuthority = roleAuthorities.get("editor");
        
        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                editorAuthority,
                List.of(new PermissionTarget(category.getId(), PermissionOperation.GRANT))
        );
        List<Permission> expectedPermissions = List.of(BasePermission.WRITE, BasePermission.CREATE);

        // when
        deviceCategoryAclService.managePermission(request);

        // then
        ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, category.getId());
        Acl acl = mutableAclService.readAclById(oi);
        assertThat(acl).isNotNull();
        
        Sid sid = new GrantedAuthoritySid(editorAuthority);
        assertThat(acl.isGranted(expectedPermissions, List.of(sid), false)).isTrue();
    }
    
    @Test
    @DisplayName("ADMIN 역할, 빈 권한 목록으로 DeviceCategory에 역할 권한 부여 시 CRUD 기본 권한 부여")
    void grantPermission_forRole_emptyPermissions_byAdmin() {
        // given
        DeviceCategory category = deviceCategories.get("main");
        String viewerAuthority = roleAuthorities.get("viewer");
        
        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                viewerAuthority,
                List.of(new PermissionTarget(category.getId(), PermissionOperation.GRANT))
        );
        List<Permission> expectedDefaultPermissions = List.of(
            BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE
        );

        // when
        deviceCategoryAclService.managePermission(request);

        // then
        ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, category.getId());
        Acl acl = mutableAclService.readAclById(oi);
        assertThat(acl).isNotNull();
        
        Sid sid = new GrantedAuthoritySid(viewerAuthority);
        
        for (Permission perm : expectedDefaultPermissions) {
            assertThat(acl.isGranted(List.of(perm), List.of(sid), false)).isTrue();
        }
    }

    @Test
    @DisplayName("ADMIN 아닌 사용자가 권한 부여 시도 시 AccessDeniedException")
    void grantPermission_byNonAdmin_thenFail() {
        // given
        DeviceCategory category = deviceCategories.get("main");
        String managerAuthority = roleAuthorities.get("manager");
        SecurityContext originalContext = SecurityContextHolder.getContext();

        try {
            // 일반 사용자로 인증 설정 (ADMIN 권한 없음)
            Authentication userAuth = new UsernamePasswordAuthenticationToken(
                managerAuthority, "password", List.of(new SimpleGrantedAuthority(managerAuthority))
            );
            SecurityContextHolder.getContext().setAuthentication(userAuth);

            PermissionRequestDto request = new PermissionRequestDto(
                    DeviceCategory.class.getSimpleName(),
                    managerAuthority,
                    List.of(new PermissionTarget(category.getId(), PermissionOperation.GRANT))
            );

            // when & then
            assertThatThrownBy(() -> deviceCategoryAclService.managePermission(request))
                .isInstanceOf(AccessDeniedException.class);
        } finally {
            SecurityContextHolder.setContext(originalContext);
        }
    }
    
    @Test
    @DisplayName("ADMIN 역할로 DeviceCategory에서 역할 권한 회수")
    void revokePermission_forRole_byAdmin() {
        // given
        DeviceCategory category = deviceCategories.get("main");
        String editorAuthority = roleAuthorities.get("editor");
        
        aclManagerService.addPermissionForRole(DeviceCategory.class, category.getId(), editorAuthority, BasePermission.READ);
        aclManagerService.addPermissionForRole(DeviceCategory.class, category.getId(), editorAuthority, BasePermission.WRITE);

        assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, category.getId(), editorAuthority, BasePermission.READ))
                .isTrue();

        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                editorAuthority,
                List.of(new PermissionTarget(category.getId(), PermissionOperation.REVOKE))
        );

        // when
        deviceCategoryAclService.managePermission(request);

        // then
        assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, category.getId(), editorAuthority, BasePermission.READ))
                .isFalse();
        assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, category.getId(), editorAuthority, BasePermission.WRITE))
                .isFalse();
    }

    @Test
    @DisplayName("타겟 타입이 DeviceCategory가 아닐 때 권한 부여 시도 시 IllegalArgumentException")
    void grantPermission_wrongTargetType_thenThrowException() {
        // given
        DeviceCategory category = deviceCategories.get("main");
        String viewerAuthority = roleAuthorities.get("viewer");
        
        PermissionRequestDto request = new PermissionRequestDto(
                "com.example.WrongType",
                viewerAuthority,
                List.of(new PermissionTarget(category.getId(), PermissionOperation.GRANT))
        );
        
        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.managePermission(request))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("READ 권한 있는 역할을 가진 사용자는 findById 호출 성공")
    void findById_withPermission_succeeds() {
        // given
        DeviceCategory category = deviceCategories.get("sub1");
        String viewerAuthority = roleAuthorities.get("viewer");
        
        // 역할에 권한 부여
        aclManagerService.addPermissionForRole(DeviceCategory.class, category.getId(), viewerAuthority, BasePermission.READ);
        
        // 해당 역할로 인증된 사용자 설정
        Authentication permittedUserAuth = new UsernamePasswordAuthenticationToken(
            viewerAuthority, "password", List.of(new SimpleGrantedAuthority(viewerAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(permittedUserAuth);

        // when
        DeviceCategory result = deviceCategoryAclService.findById(category.getId());
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(category.getId());
        assertThat(result.getName()).isEqualTo("서브 카테고리 1");
    }

    @Test
    @DisplayName("READ 권한 없는 역할을 가진 사용자는 findById 호출 시 AccessDeniedException")
    void findById_withoutPermission_fails() {
        // given
        DeviceCategory category = deviceCategories.get("sub2");
        String guestAuthority = roleAuthorities.get("guest");
        
        // 권한 없는 사용자로 인증
        Authentication unpermittedUserAuth = new UsernamePasswordAuthenticationToken(
            guestAuthority, "password", List.of(new SimpleGrantedAuthority(guestAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(unpermittedUserAuth);

        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.findById(category.getId()))
            .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("hasReadPermission: 권한 있는 역할을 가진 사용자는 true 반환")
    void hasReadPermission_withPermission_returnsTrue() {
        // given
        DeviceCategory category = deviceCategories.get("independent");
        String managerAuthority = roleAuthorities.get("manager");
        
        aclManagerService.addPermissionForRole(DeviceCategory.class, category.getId(), managerAuthority, BasePermission.READ);
        
        Authentication permittedUserAuth = new UsernamePasswordAuthenticationToken(
            managerAuthority, "password", List.of(new SimpleGrantedAuthority(managerAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(permittedUserAuth);

        // when
        boolean hasPermission = deviceCategoryAclService.hasReadPermission(category.getId());
        
        // then
        assertThat(hasPermission).isTrue();
    }
    
    @Test
    @DisplayName("hasReadPermission: 권한 없는 역할을 가진 사용자는 false 반환")
    void hasReadPermission_withoutPermission_returnsFalse() {
        // given
        DeviceCategory category = deviceCategories.get("nested");
        String guestAuthority = roleAuthorities.get("guest");
        
        Authentication unpermittedUserAuth = new UsernamePasswordAuthenticationToken(
            guestAuthority, "password", List.of(new SimpleGrantedAuthority(guestAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(unpermittedUserAuth);

        // when
        boolean hasPermission = deviceCategoryAclService.hasReadPermission(category.getId());
        
        // then
        assertThat(hasPermission).isFalse();
    }
    
    @Test
    @DisplayName("findAllAllowedForCurrentUser: ADMIN 사용자는 모든 항목 조회 가능")
    void findAllAllowedForCurrentUser_asAdmin_returnsAllItems() {
        // given - ADMIN 권한은 이미 setUp에서 설정됨
        
        // when
        List<DeviceCategoryResponseDto> result = deviceCategoryAclService.findAllAllowedForCurrentUser();
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(deviceCategories.size());
        
        // 모든 카테고리 ID가 결과에 포함되어 있는지 확인
        List<Long> allCategoryIds = deviceCategories.values().stream()
            .map(DeviceCategory::getId)
            .collect(Collectors.toList());
        
        List<Long> resultIds = result.stream()
            .map(DeviceCategoryResponseDto::id)
            .collect(Collectors.toList());
            
        assertThat(resultIds).containsAll(allCategoryIds);
    }
    
    @Test
    @DisplayName("findAllAllowedForCurrentUser: 일반 사용자는 권한 있는 항목만 조회 가능")
    void findAllAllowedForCurrentUser_asUser_returnsOnlyAllowedItems() {
        // given
        DeviceCategory mainCategory = deviceCategories.get("main");
        DeviceCategory subCategory1 = deviceCategories.get("sub1");
        String editorAuthority = roleAuthorities.get("editor");
        
        // 2개의 카테고리에만 권한 부여
        aclManagerService.addPermissionForRole(DeviceCategory.class, mainCategory.getId(), editorAuthority, BasePermission.READ);
        aclManagerService.addPermissionForRole(DeviceCategory.class, subCategory1.getId(), editorAuthority, BasePermission.READ);
        
        // 일반 사용자로 설정
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
            editorAuthority, "password", List.of(new SimpleGrantedAuthority(editorAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        
        // when
        List<DeviceCategoryResponseDto> result = deviceCategoryAclService.findAllAllowedForCurrentUser();
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        
        List<Long> expectedIds = List.of(mainCategory.getId(), subCategory1.getId());
        List<Long> resultIds = result.stream()
            .map(DeviceCategoryResponseDto::id)
            .collect(Collectors.toList());
            
        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(expectedIds);
    }

    @Test
    @DisplayName("ACL이 없는 객체에 대한 hasReadPermission 테스트")
    void hasReadPermission_nonExistentAcl_returnsFalse() {
        // given
        Long nonExistentId = 999999L;
        String editorAuthority = roleAuthorities.get("editor");
        
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
            editorAuthority, "password", List.of(new SimpleGrantedAuthority(editorAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        
        // when
        boolean hasPermission = deviceCategoryAclService.hasReadPermission(nonExistentId);
        
        // then
        assertThat(hasPermission).isFalse();
    }
    
    @Test
    @DisplayName("ACL이 없는 객체에 대한 findById 접근 시 AccessDeniedException")
    void findById_nonExistentAcl_throwsAccessDeniedException() {
        // given
        Long nonExistentId = 999999L;
        String viewerAuthority = roleAuthorities.get("viewer");
        
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
            viewerAuthority, "password", List.of(new SimpleGrantedAuthority(viewerAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        
        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.findById(nonExistentId))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("ADMIN 역할의 권한 회수 시도 시 예외 발생 테스트")
    void revokePermission_forAdminRole_throwsException() {
        // given
        DeviceCategory category = deviceCategories.get("main");
        String adminRole = "ROLE_ADMIN";
        List<Permission> permissionsToGrant = List.of(
            BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE, BasePermission.ADMINISTRATION
        );
        aclManagerService.addPermissionsForRole(DeviceCategory.class, category.getId(), adminRole, permissionsToGrant);

        for(Permission p : permissionsToGrant) {
            assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, category.getId(), adminRole, p))
                .isTrue();
        }

        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                adminRole,
                List.of(new PermissionTarget(category.getId(), PermissionOperation.REVOKE))
        );
        
        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.managePermission(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ADMIN 역할의 권한은 회수할 수 없습니다");
        
        // 권한이 실제로 회수되지 않았는지 확인
        for(Permission p : permissionsToGrant) {
            assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, category.getId(), adminRole, p))
                .isTrue();
        }
    }
    
    @Test
    @DisplayName("다수의 관리자와 역할이 있는 환경에서 권한 관리 테스트")
    void multipleAdminsAndRoles_permissionManagement() {
        // given
        DeviceCategory mainCategory = deviceCategories.get("main");
        DeviceCategory subCategory1 = deviceCategories.get("sub1");
        DeviceCategory subCategory2 = deviceCategories.get("sub2");
        
        String superAdminAuthority = roleAuthorities.get("superAdmin");
        String systemAdminAuthority = roleAuthorities.get("systemAdmin");
        String editorAuthority = roleAuthorities.get("editor");
        String viewerAuthority = roleAuthorities.get("viewer");
        
        // 슈퍼 관리자로 인증 설정
        Authentication superAdminAuth = new UsernamePasswordAuthenticationToken(
            superAdminAuthority, "password", List.of(
                new SimpleGrantedAuthority(superAdminAuthority),
                new SimpleGrantedAuthority("ROLE_ADMIN") // 관리자 권한 부여
            )
        );
        SecurityContextHolder.getContext().setAuthentication(superAdminAuth);
        
        // 다양한 역할에 다양한 권한 부여
        // 1. 시스템 관리자에게 메인 카테고리에 대한 모든 권한 부여
        PermissionRequestDto request1 = new PermissionRequestDto(
            DeviceCategory.class.getSimpleName(),
            systemAdminAuthority,
            List.of(new PermissionTarget(mainCategory.getId(), PermissionOperation.GRANT))
        );
        deviceCategoryAclService.managePermission(request1);
        
        // 2. 편집자에게 서브 카테고리 1에 대한 권한 부여
        PermissionRequestDto request2 = new PermissionRequestDto(
            DeviceCategory.class.getSimpleName(),
            editorAuthority,
            List.of(new PermissionTarget(subCategory1.getId(), PermissionOperation.GRANT))
        );
        deviceCategoryAclService.managePermission(request2);
        
        // 3. 뷰어에게 서브 카테고리 2에 대한 권한 부여
        PermissionRequestDto request3 = new PermissionRequestDto(
            DeviceCategory.class.getSimpleName(),
            viewerAuthority,
            List.of(new PermissionTarget(subCategory2.getId(), PermissionOperation.GRANT))
        );
        deviceCategoryAclService.managePermission(request3);
        
        // then - 각 역할별 권한 확인
        // 시스템 관리자 권한 확인
        Authentication systemAdminAuth = new UsernamePasswordAuthenticationToken(
            systemAdminAuthority, "password", List.of(new SimpleGrantedAuthority(systemAdminAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(systemAdminAuth);
        
        assertThat(deviceCategoryAclService.hasReadPermission(mainCategory.getId())).isTrue();
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory1.getId())).isFalse();
        
        // 편집자 권한 확인
        Authentication editorAuth = new UsernamePasswordAuthenticationToken(
            editorAuthority, "password", List.of(new SimpleGrantedAuthority(editorAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(editorAuth);
        
        assertThat(deviceCategoryAclService.hasReadPermission(mainCategory.getId())).isFalse();
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory1.getId())).isTrue();
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory2.getId())).isFalse();
        
        // 뷰어 권한 확인
        Authentication viewerAuth = new UsernamePasswordAuthenticationToken(
            viewerAuthority, "password", List.of(new SimpleGrantedAuthority(viewerAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(viewerAuth);
        
        assertThat(deviceCategoryAclService.hasReadPermission(mainCategory.getId())).isFalse();
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory1.getId())).isFalse();
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory2.getId())).isTrue();
    }
    
    @Test
    @DisplayName("여러 개의 DeviceCategory에 대한 일괄 권한 부여 및 확인")
    void batchPermissionGrantTest() {
        // given
        DeviceCategory mainCategory = deviceCategories.get("main");
        DeviceCategory subCategory1 = deviceCategories.get("sub1");
        DeviceCategory subCategory2 = deviceCategories.get("sub2");
        String managerAuthority = roleAuthorities.get("manager");
        
        // 다수의 카테고리에 한 번에 권한 부여
        List<PermissionTarget> targets = List.of(
            new PermissionTarget(mainCategory.getId(), PermissionOperation.GRANT),
            new PermissionTarget(subCategory1.getId(), PermissionOperation.GRANT),
            new PermissionTarget(subCategory2.getId(), PermissionOperation.GRANT)
        );
        
        PermissionRequestDto request = new PermissionRequestDto(
            DeviceCategory.class.getSimpleName(),
            managerAuthority,
            targets
        );
        
        // when
        deviceCategoryAclService.managePermission(request);
        
        // then
        Authentication managerAuth = new UsernamePasswordAuthenticationToken(
            managerAuthority, "password", List.of(new SimpleGrantedAuthority(managerAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(managerAuth);
        
        // 모든 카테고리에 대해 권한이 부여되었는지 확인
        assertThat(deviceCategoryAclService.hasReadPermission(mainCategory.getId())).isTrue();
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory1.getId())).isTrue();
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory2.getId())).isTrue();
        
        // findAllAllowedForCurrentUser로도 확인
        List<DeviceCategoryResponseDto> result = deviceCategoryAclService.findAllAllowedForCurrentUser();
        List<Long> resultIds = result.stream()
            .map(DeviceCategoryResponseDto::id)
            .collect(Collectors.toList());
            
        assertThat(resultIds).contains(mainCategory.getId(), subCategory1.getId(), subCategory2.getId());
    }
    
    @Test
    @DisplayName("계층 구조의 DeviceCategory에서 부모-자식 관계와 권한 상속 테스트")
    void hierarchicalCategoryPermissionInheritanceTest() {
        // given
        DeviceCategory mainCategory = deviceCategories.get("main");
        DeviceCategory subCategory1 = deviceCategories.get("sub1");
        DeviceCategory nestedCategory = deviceCategories.get("nested");
        String editorAuthority = roleAuthorities.get("editor");
        
        // 부모 카테고리에만 권한 부여
        aclManagerService.addPermissionForRole(DeviceCategory.class, mainCategory.getId(), editorAuthority, BasePermission.READ);
        
        // 중첩 카테고리에만 권한 부여
        aclManagerService.addPermissionForRole(DeviceCategory.class, nestedCategory.getId(), editorAuthority, BasePermission.READ);
        
        Authentication editorAuth = new UsernamePasswordAuthenticationToken(
            editorAuthority, "password", List.of(new SimpleGrantedAuthority(editorAuthority))
        );
        SecurityContextHolder.getContext().setAuthentication(editorAuth);
        
        // when & then
        // 권한이 있는 카테고리들 확인
        assertThat(deviceCategoryAclService.hasReadPermission(mainCategory.getId())).isTrue();
        assertThat(deviceCategoryAclService.hasReadPermission(nestedCategory.getId())).isTrue();
        
        // 명시적으로 권한을 주지 않은 중간 카테고리는 권한 없음
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory1.getId())).isFalse();
        
        // findAllAllowedForCurrentUser 호출 시 권한 있는 카테고리만 반환
        List<DeviceCategoryResponseDto> result = deviceCategoryAclService.findAllAllowedForCurrentUser();
        List<Long> resultIds = result.stream()
            .map(DeviceCategoryResponseDto::id)
            .collect(Collectors.toList());
            
        assertThat(resultIds).contains(mainCategory.getId(), nestedCategory.getId());
        assertThat(resultIds).doesNotContain(subCategory1.getId());
        
        // 명시적으로 중간 카테고리에 권한 부여
        aclManagerService.addPermissionForRole(DeviceCategory.class, subCategory1.getId(), editorAuthority, BasePermission.READ);
        
        // 모든 계층에 대한 권한 확인
        assertThat(deviceCategoryAclService.hasReadPermission(mainCategory.getId())).isTrue();
        assertThat(deviceCategoryAclService.hasReadPermission(subCategory1.getId())).isTrue();
        assertThat(deviceCategoryAclService.hasReadPermission(nestedCategory.getId())).isTrue();
    }
} 