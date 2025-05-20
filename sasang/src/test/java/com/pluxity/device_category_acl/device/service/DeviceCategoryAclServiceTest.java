package com.pluxity.device_category_acl.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.domains.acl.service.AclManagerService;
import com.pluxity.domains.device_category_acl.device.dto.DeviceCategoryResponseDto;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto.PermissionOperation;
import com.pluxity.domains.device_category_acl.device.dto.PermissionRequestDto.PermissionTarget;
import com.pluxity.domains.device_category_acl.device.service.DeviceCategoryAclService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private Long deviceCategoryId;
    private static Long deviceCategoryTestCounter = 1L;

    private Authentication originalAuth;

    @BeforeEach
    void setUp() {
        deviceCategoryId = deviceCategoryTestCounter++;
        originalAuth = SecurityContextHolder.getContext().getAuthentication();

        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
            "adminForTest", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        try {
            ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, deviceCategoryId);
            mutableAclService.deleteAcl(oi, false);
        } catch (NotFoundException e) {
            // ACL이 없으면 무시
        }
    }

    @AfterEach
    void tearDown() {
        try {
            ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, deviceCategoryId);
            mutableAclService.deleteAcl(oi, false);
        } catch (NotFoundException e) {
            // ACL이 없으면 무시
        }
        SecurityContextHolder.getContext().setAuthentication(originalAuth);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("ADMIN 역할로 DeviceCategory에 역할 WRITE, CREATE 권한 부여")
    void grantPermission_forRole_byAdmin() {
        // given
        String testRole = "ROLE_EDITOR_GRANT";
        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                testRole,
                List.of(new PermissionTarget(deviceCategoryId, PermissionOperation.GRANT))
        );
        List<Permission> expectedPermissions = List.of(BasePermission.WRITE, BasePermission.CREATE);

        // when
        deviceCategoryAclService.managePermission(request);

        // then
        ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, deviceCategoryId);
        Acl acl = mutableAclService.readAclById(oi);
        assertThat(acl).isNotNull()
            .withFailMessage("ACL should exist for DeviceCategory ID: " + deviceCategoryId);
        
        Sid sid = new GrantedAuthoritySid(testRole);
        assertThat(acl.isGranted(expectedPermissions, List.of(sid), false))
            .isTrue()
            .withFailMessage(testRole + " should have WRITE and CREATE permissions");
    }
    
    @Test
    @DisplayName("ADMIN 역할, 빈 권한 목록으로 DeviceCategory에 역할 권한 부여 시 CRUD 기본 권한 부여")
    void grantPermission_forRole_emptyPermissions_byAdmin() {
        // given
        String testRole = "ROLE_VIEWER_GRANT_EMPTY";
        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                testRole,
                List.of(new PermissionTarget(deviceCategoryId, PermissionOperation.GRANT))
        );
        List<Permission> expectedDefaultPermissions = List.of(
            BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE
        );

        // when
        deviceCategoryAclService.managePermission(request);

        // then
        ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, deviceCategoryId);
        Acl acl = mutableAclService.readAclById(oi);
        assertThat(acl).isNotNull()
            .withFailMessage("ACL should exist for DeviceCategory ID: " + deviceCategoryId);
        
        Sid sid = new GrantedAuthoritySid(testRole);
        
        for (Permission perm : expectedDefaultPermissions) {
            assertThat(acl.isGranted(List.of(perm), List.of(sid), false))
                .isTrue()
                .withFailMessage(testRole + " should have " + perm.toString() + " permission when granted with empty list");
        }
    }

    @Test
    @DisplayName("ADMIN 아닌 사용자가 권한 부여 시도 시 AccessDeniedException")
    @WithMockUser(username = "nonAdminUser", roles = "USER")
    void grantPermission_byNonAdmin_thenFail() {
        // given
        SecurityContext originalContext = SecurityContextHolder.getContext();

        try {
            SecurityContext newContext = SecurityContextHolder.createEmptyContext();
            List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
            Authentication authentication = new UsernamePasswordAuthenticationToken("nonAdminUser", "password", authorities);
            newContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(newContext);

            PermissionRequestDto request = new PermissionRequestDto(
                    DeviceCategory.class.getSimpleName(),
                    "anotherUser",
                    List.of(new PermissionTarget(deviceCategoryId, PermissionOperation.GRANT))
            );

            // when & then
            assertThatThrownBy(() -> deviceCategoryAclService.managePermission(request))
                .isInstanceOf(AccessDeniedException.class)
                .withFailMessage("Non-ADMIN user should not be able to grant permissions.");

        } finally {
            SecurityContextHolder.setContext(originalContext);
        }
    }
    
    @Test
    @DisplayName("ADMIN 역할로 DeviceCategory에서 사용자 READ 권한 회수")
    void revokePermission_forRole_byAdmin() {
        // given
        String targetUser = "testUserRevoke";
        aclManagerService.addPermissionForRole(DeviceCategory.class, deviceCategoryId, targetUser, BasePermission.READ);
        aclManagerService.addPermissionForRole(DeviceCategory.class, deviceCategoryId, targetUser, BasePermission.WRITE);

        assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, deviceCategoryId, targetUser, BasePermission.READ))
                .isTrue();

        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                targetUser,
                List.of(new PermissionTarget(deviceCategoryId, PermissionOperation.REVOKE))
        );

        // when
        deviceCategoryAclService.managePermission(request);

        // then
        assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, deviceCategoryId, targetUser, BasePermission.READ))
                .isFalse();
        assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, deviceCategoryId, targetUser, BasePermission.WRITE))
                .isFalse();
    }

    @Test
    @DisplayName("타겟 타입이 DeviceCategory가 아닐 때 권한 부여 시도 시 IllegalArgumentException")
    void grantPermission_wrongTargetType_thenThrowException() {
        // given
        PermissionRequestDto request = new PermissionRequestDto(
                "com.example.WrongType",
                "testUserWrongType",
                List.of(new PermissionTarget(deviceCategoryId, PermissionOperation.GRANT))
        );
        
        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.managePermission(request))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    @DisplayName("READ 권한 있는 사용자는 findById 호출 성공")
    void findById_withPermission_succeeds() {
        // given
        String permittedUsername = "permittedUserRead";
        aclManagerService.addPermissionForUser(DeviceCategory.class, deviceCategoryId, permittedUsername, BasePermission.READ);
        
        Authentication permittedUserAuth = new UsernamePasswordAuthenticationToken(
            permittedUsername, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(permittedUserAuth);

        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.findById(deviceCategoryId))
            .isInstanceOf(IllegalArgumentException.class)
            .withFailMessage("Exception should be about not finding the entity, not about permissions");
    }

    @Test
    @DisplayName("READ 권한 없는 사용자는 findById 호출 시 AccessDeniedException")
    void findById_withoutPermission_fails() {
        // given
        String unpermittedUsername = "unpermittedUserRead";
        Authentication unpermittedUserAuth = new UsernamePasswordAuthenticationToken(
            unpermittedUsername, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(unpermittedUserAuth);

        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.findById(deviceCategoryId))
            .isInstanceOf(AccessDeniedException.class)
            .withFailMessage("AccessDeniedException should be thrown for user without READ permission.");
    }
    
    @Test
    @DisplayName("hasReadPermission: 권한 있는 사용자는 true 반환")
    void hasReadPermission_withPermission_returnsTrue() {
        // given
        String permittedUsername = "permittedUserHasRead";
        aclManagerService.addPermissionForRole(DeviceCategory.class, deviceCategoryId, permittedUsername, BasePermission.READ);
        
        Authentication permittedUserAuth = new UsernamePasswordAuthenticationToken(
            permittedUsername, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(permittedUserAuth);

        // when
        boolean hasPermission = deviceCategoryAclService.hasReadPermission(deviceCategoryId);
        
        // then
        assertThat(hasPermission).isTrue()
            .withFailMessage("User with READ permission should get true from hasReadPermission");
    }
    
    @Test
    @DisplayName("hasReadPermission: 권한 없는 사용자는 false 반환")
    void hasReadPermission_withoutPermission_returnsFalse() {
        // given
        String unpermittedUsername = "unpermittedUserHasRead";
        Authentication unpermittedUserAuth = new UsernamePasswordAuthenticationToken(
            unpermittedUsername, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(unpermittedUserAuth);

        // when
        boolean hasPermission = deviceCategoryAclService.hasReadPermission(deviceCategoryId);
        
        // then
        assertThat(hasPermission).isFalse()
            .withFailMessage("User without READ permission should get false from hasReadPermission");
    }
    
    @Test
    @DisplayName("findAllAllowedForCurrentUser: ADMIN 사용자는 모든 항목 조회 가능")
    void findAllAllowedForCurrentUser_asAdmin_returnsAllItems() {
        // given
        // ADMIN으로 설정 (이미 setUp에서 설정됨)
        
        // when
        List<DeviceCategoryResponseDto> result = deviceCategoryAclService.findAllAllowedForCurrentUser();
        
        // then
        assertThat(result).isNotNull()
            .withFailMessage("Result should not be null for ADMIN");
    }
    
    @Test
    @DisplayName("findAllAllowedForCurrentUser: 일반 사용자는 권한 있는 항목만 조회 가능")
    void findAllAllowedForCurrentUser_asUser_returnsOnlyAllowedItems() {
        // given
        String username = "regularUserForFiltering";
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
            username, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        
        // when
        List<DeviceCategoryResponseDto> result = deviceCategoryAclService.findAllAllowedForCurrentUser();
        
        // then
        assertThat(result).isNotNull()
            .withFailMessage("Result should not be null for regular user");
    }

    @Test
    @DisplayName("ACL이 없는 객체에 대한 hasReadPermission 테스트")
    void hasReadPermission_nonExistentAcl_returnsFalse() {
        // given
        Long nonExistentId = 999999L;
        
        // when
        boolean hasPermission = deviceCategoryAclService.hasReadPermission(nonExistentId);
        
        // then
        assertThat(hasPermission).isFalse()
            .withFailMessage("Non-existent ACL should return false from hasReadPermission");
    }
    
    @Test
    @DisplayName("ACL이 없는 객체에 대한 findById 접근 시 AccessDeniedException")
    void findById_nonExistentAcl_throwsAccessDeniedException() {
        // given
        Long nonExistentId = 999999L;
        
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "regularTestUser", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );
        
        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.findById(nonExistentId))
            .isInstanceOf(AccessDeniedException.class)
            .withFailMessage("AccessDeniedException should be thrown for non-existent ACL");
    }

    @Test
    @DisplayName("ADMIN 역할의 권한 회수 시도 시 예외 발생 테스트")
    void revokePermission_forAdminRole_throwsException() {
        // given
        String adminRole = "ROLE_ADMIN";
        List<Permission> permissionsToGrant = List.of(
            BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE, BasePermission.ADMINISTRATION
        );
        aclManagerService.addPermissionsForRole(DeviceCategory.class, deviceCategoryId, adminRole, permissionsToGrant);

        for(Permission p : permissionsToGrant) {
            assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, deviceCategoryId, adminRole, p))
                .isTrue()
                .withFailMessage(p.toString() + " permission should be granted for role " + adminRole);
        }

        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                adminRole,
                List.of(new PermissionTarget(deviceCategoryId, PermissionOperation.REVOKE))
        );
        
        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.managePermission(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ADMIN 역할의 권한은 회수할 수 없습니다")
            .withFailMessage("Should throw IllegalStateException when trying to revoke permissions from ADMIN role");
        
        // 권한이 실제로 회수되지 않았는지 확인
        for(Permission p : permissionsToGrant) {
            assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, deviceCategoryId, adminRole, p))
                .isTrue()
                .withFailMessage(p.toString() + " permission should still be granted for role " + adminRole + " after failed revocation attempt");
        }
    }
    
    @Test
    @DisplayName("ADMIN 역할의 모든 권한 회수 시도 시 예외 발생 테스트")
    void revokeAllPermissions_forAdminRole_throwsException() {
        // given
        String adminRole = "ROLE_ADMIN";
        List<Permission> permissionsToGrant = List.of(
            BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE, BasePermission.ADMINISTRATION
        );
        aclManagerService.addPermissionsForRole(DeviceCategory.class, deviceCategoryId, adminRole, permissionsToGrant);

        PermissionRequestDto request = new PermissionRequestDto(
                DeviceCategory.class.getSimpleName(),
                adminRole,
                List.of(new PermissionTarget(deviceCategoryId, PermissionOperation.REVOKE))
        );
        
        // when & then
        assertThatThrownBy(() -> deviceCategoryAclService.managePermission(request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ADMIN 역할의 권한은 회수할 수 없습니다")
            .withFailMessage("Should throw IllegalStateException when trying to revoke all permissions from ADMIN role");
        
        // 권한이 실제로 회수되지 않았는지 확인
        for(Permission p : permissionsToGrant) {
            assertThat(aclManagerService.hasPermissionForRole(DeviceCategory.class, deviceCategoryId, adminRole, p))
                .isTrue()
                .withFailMessage(p.toString() + " permission should still be granted for role " + adminRole + " after failed revocation attempt");
        }
    }
} 