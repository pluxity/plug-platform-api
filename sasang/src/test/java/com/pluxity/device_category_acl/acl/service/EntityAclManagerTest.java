package com.pluxity.device_category_acl.acl.service;

import com.pluxity.SasangApplication;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.domains.acl.service.AclManagerService;
import com.pluxity.domains.acl.service.EntityAclManager;
import com.pluxity.domains.device_category_acl.device.dto.GrantPermissionRequest;
import com.pluxity.domains.device_category_acl.device.dto.RevokePermissionRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = SasangApplication.class)
@Transactional
@ActiveProfiles("test")
class EntityAclManagerTest {

    @Autowired
    private AclManagerService aclManagerService;
    
    @Autowired
    private MutableAclService mutableAclService;
    
    private EntityAclManager entityAclManager;
    private final Long testEntityId = 1L;
    private final String testEntityType = "DeviceCategory";
    private final Class<?> testEntityClass = DeviceCategory.class;
    private Authentication originalAuth;

    @BeforeEach
    void setUp() {
        // given
        entityAclManager = new EntityAclManager(aclManagerService);
        
        // 테스트를 위한 인증 정보 저장
        originalAuth = SecurityContextHolder.getContext().getAuthentication();
        
        // ADMIN 권한으로 테스트 진행
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
            "adminForTest", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);
        
        // 테스트 전 해당 ID로 기존 ACL이 있다면 정리
        try {
            ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, testEntityId);
            mutableAclService.deleteAcl(oi, false);
        } catch (NotFoundException e) {
            // ACL이 없으면 무시
        }
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 해당 ID의 ACL 정리
        try {
            ObjectIdentity oi = new ObjectIdentityImpl(DeviceCategory.class, testEntityId);
            mutableAclService.deleteAcl(oi, false);
        } catch (NotFoundException e) {
            // ACL이 없으면 무시
        }
        SecurityContextHolder.getContext().setAuthentication(originalAuth);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("validateEntityType: 일치하는 엔티티 타입은 예외 없이 통과")
    void validateEntityType_matchingTypes_noException() {
        // given
        String requestType = testEntityType;
        
        // when & then
        entityAclManager.validateEntityType(requestType, testEntityType);
        // 예외가 발생하지 않으면 테스트 통과
    }

    @Test
    @DisplayName("validateEntityType: 일치하지 않는 엔티티 타입은 예외 발생")
    void validateEntityType_nonMatchingTypes_throwsException() {
        // given
        String wrongType = "WrongType";
        
        // when & then
        assertThatThrownBy(() -> entityAclManager.validateEntityType(wrongType, testEntityType))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Target type must be");
    }

    @Test
    @DisplayName("convertToPermissions: 권한 문자열 목록이 Permission 객체로 변환됨")
    void convertToPermissions_validPermissions_returnsPermissionList() {
        // given
        List<String> permissionStrings = List.of("READ", "WRITE", "CREATE");
        
        // when
        List<Permission> result = entityAclManager.convertToPermissions(permissionStrings);
        
        // then
        assertThat(result).hasSize(3);
        assertThat(result).contains(BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE);
    }

    @Test
    @DisplayName("convertToPermissions: 빈 권한 목록은 기본 CRUD 권한을 반환함")
    void convertToPermissions_emptyPermissions_returnsDefaultPermissions() {
        // given
        List<String> emptyPermissions = Collections.emptyList();
        
        // when
        List<Permission> result = entityAclManager.convertToPermissions(emptyPermissions);
        
        // then
        assertThat(result).hasSize(4);
        assertThat(result).contains(
            BasePermission.READ, 
            BasePermission.WRITE, 
            BasePermission.CREATE, 
            BasePermission.DELETE
        );
    }
    
    @Test
    @DisplayName("convertToPermissions: null 권한 목록은 기본 CRUD 권한을 반환함")
    void convertToPermissions_nullPermissions_returnsDefaultPermissions() {
        // given
        List<String> nullPermissions = null;
        
        // when
        List<Permission> result = entityAclManager.convertToPermissions(nullPermissions);
        
        // then
        assertThat(result).hasSize(4);
        assertThat(result).contains(
            BasePermission.READ, 
            BasePermission.WRITE, 
            BasePermission.CREATE, 
            BasePermission.DELETE
        );
    }
    
    @Test
    @DisplayName("grantPermission: 사용자에게 권한 부여 시 권한이 정상적으로 추가됨")
    void grantPermission_forUser_grantsPermissions() {
        // given
        String testUsername = "testUser";
        GrantPermissionRequest request = new GrantPermissionRequest(
            testEntityType, testEntityId, testUsername, false, List.of("READ", "WRITE")
        );
        
        // when
        entityAclManager.grantPermission(request, testEntityType, testEntityClass);
        
        // then
        assertThat(aclManagerService.hasPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.READ))
            .isTrue();
        assertThat(aclManagerService.hasPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.WRITE))
            .isTrue();
    }
    
    @Test
    @DisplayName("grantPermission: 역할에게 권한 부여 시 권한이 정상적으로 추가됨")
    void grantPermission_forRole_grantsPermissions() {
        // given
        String testRole = "ROLE_EDITOR";
        GrantPermissionRequest request = new GrantPermissionRequest(
            testEntityType, testEntityId, testRole, true, List.of("READ", "WRITE")
        );
        
        // when
        entityAclManager.grantPermission(request, testEntityType, testEntityClass);
        
        // then
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, testRole, BasePermission.READ))
            .isTrue();
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, testRole, BasePermission.WRITE))
            .isTrue();
    }
    
    @Test
    @DisplayName("grantPermission: 잘못된 엔티티 타입으로 권한 부여 시도 시 예외 발생")
    void grantPermission_wrongEntityType_throwsException() {
        // given
        GrantPermissionRequest request = new GrantPermissionRequest(
            "WrongType", testEntityId, "testUser", false, List.of("READ")
        );
        
        // when & then
        assertThatThrownBy(() -> entityAclManager.grantPermission(request, testEntityType, testEntityClass))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Target type must be");
    }
    
    @Test
    @DisplayName("revokePermission: 사용자의 모든 권한 회수 시 권한이 제거됨")
    void revokePermission_removeAllForUser_revokesAllPermissions() {
        // given
        String testUsername = "testUserRevoke";
        // 먼저 권한 부여
        aclManagerService.addPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.READ);
        aclManagerService.addPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.WRITE);
        
        RevokePermissionRequest request = new RevokePermissionRequest(
            testEntityType, testEntityId, testUsername, false, Collections.emptyList(), true
        );
        
        // when
        entityAclManager.revokePermission(request, testEntityType, testEntityClass);
        
        // then
        assertThat(aclManagerService.hasPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.READ))
            .isFalse();
        assertThat(aclManagerService.hasPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.WRITE))
            .isFalse();
    }
    
    @Test
    @DisplayName("revokePermission: 역할의 모든 권한 회수 시 권한이 제거됨")
    void revokePermission_removeAllForRole_revokesAllPermissions() {
        // given
        String testRole = "ROLE_EDITOR_REVOKE";
        // 먼저 권한 부여
        aclManagerService.addPermissionForRole(testEntityClass, testEntityId, testRole, BasePermission.READ);
        aclManagerService.addPermissionForRole(testEntityClass, testEntityId, testRole, BasePermission.WRITE);
        
        RevokePermissionRequest request = new RevokePermissionRequest(
            testEntityType, testEntityId, testRole, true, Collections.emptyList(), true
        );
        
        // when
        entityAclManager.revokePermission(request, testEntityType, testEntityClass);
        
        // then
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, testRole, BasePermission.READ))
            .isFalse();
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, testRole, BasePermission.WRITE))
            .isFalse();
    }
    
    @Test
    @DisplayName("revokePermission: 사용자의 특정 권한만 회수 시 해당 권한만 제거됨")
    void revokePermission_specificPermissionsForUser_revokesOnlySpecifiedPermissions() {
        // given
        String testUsername = "testUserSpecificRevoke";
        // 먼저 권한 부여
        aclManagerService.addPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.READ);
        aclManagerService.addPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.WRITE);
        
        RevokePermissionRequest request = new RevokePermissionRequest(
            testEntityType, testEntityId, testUsername, false, List.of("READ"), false
        );
        
        // when
        entityAclManager.revokePermission(request, testEntityType, testEntityClass);
        
        // then
        assertThat(aclManagerService.hasPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.READ))
            .isFalse();
        assertThat(aclManagerService.hasPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.WRITE))
            .isTrue();
    }
    
    @Test
    @DisplayName("revokePermission: 빈 권한 목록으로 권한 회수 시 모든 권한 제거")
    void revokePermission_emptyPermissionList_revokesAllPermissions() {
        // given
        String testUsername = "testUserEmptyRevoke";
        // 먼저 권한 부여
        aclManagerService.addPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.READ);
        aclManagerService.addPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.WRITE);
        
        RevokePermissionRequest request = new RevokePermissionRequest(
            testEntityType, testEntityId, testUsername, false, Collections.emptyList(), false
        );
        
        // when
        entityAclManager.revokePermission(request, testEntityType, testEntityClass);
        
        // then
        assertThat(aclManagerService.hasPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.READ))
            .isFalse();
        assertThat(aclManagerService.hasPermissionForUser(testEntityClass, testEntityId, testUsername, BasePermission.WRITE))
            .isFalse();
    }
    
    @Test
    @DisplayName("findAllAllowedForCurrentUser: ADMIN 사용자는 모든 항목에 접근 가능")
    void findAllAllowedForCurrentUser_adminUser_returnsAllEntities() {
        // given
        TestEntity entity1 = new TestEntity(1L, "Entity 1");
        TestEntity entity2 = new TestEntity(2L, "Entity 2");
        List<TestEntity> entities = List.of(entity1, entity2);
        
        Function<TestEntity, Long> idExtractor = TestEntity::getId;
        Function<TestEntity, String> dtoConverter = TestEntity::getName;
        
        // when
        List<String> result = entityAclManager.findAllAllowedForCurrentUser(
            entities, idExtractor, dtoConverter, testEntityClass
        );
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains("Entity 1", "Entity 2");
    }
    
    @Test
    @DisplayName("findAllAllowedForCurrentUser: 일반 사용자는 권한 있는 항목만 접근 가능")
    void findAllAllowedForCurrentUser_regularUser_returnsOnlyAllowedEntities() {
        // given
        TestEntity entity1 = new TestEntity(1L, "Entity 1");
        TestEntity entity2 = new TestEntity(2L, "Entity 2");
        List<TestEntity> entities = List.of(entity1, entity2);
        
        Function<TestEntity, Long> idExtractor = TestEntity::getId;
        Function<TestEntity, String> dtoConverter = TestEntity::getName;
        
        String regularUsername = "regularUser";
        // 일반 사용자로 변경
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                regularUsername, "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
            )
        );
        
        // entity1에만 접근 권한 부여
        aclManagerService.addPermissionForUser(testEntityClass, 1L, regularUsername, BasePermission.READ);
        
        // when
        List<String> result = entityAclManager.findAllAllowedForCurrentUser(
            entities, idExtractor, dtoConverter, testEntityClass
        );
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains("Entity 1");
        assertThat(result).doesNotContain("Entity 2");
    }
    
    @Test
    @DisplayName("findAllAllowedForCurrentUser: 인증 정보가 없는 경우 빈 목록 반환")
    void findAllAllowedForCurrentUser_noAuthentication_returnsEmptyList() {
        // given
        TestEntity entity1 = new TestEntity(1L, "Entity 1");
        List<TestEntity> entities = List.of(entity1);
        
        Function<TestEntity, Long> idExtractor = TestEntity::getId;
        Function<TestEntity, String> dtoConverter = TestEntity::getName;
        
        // 인증 정보 제거
        SecurityContextHolder.clearContext();
        
        // when
        List<String> result = entityAclManager.findAllAllowedForCurrentUser(
            entities, idExtractor, dtoConverter, testEntityClass
        );
        
        // then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("hasReadPermission: 사용자가 읽기 권한 있는 경우 true 반환")
    void hasReadPermission_userHasPermission_returnsTrue() {
        // given
        String username = "testUserHasRead";
        aclManagerService.addPermissionForUser(testEntityClass, testEntityId, username, BasePermission.READ);
        
        // when
        boolean result = entityAclManager.hasReadPermission(testEntityId, testEntityClass, username);
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("hasReadPermission: 사용자가 읽기 권한 없는 경우 false 반환")
    void hasReadPermission_userDoesNotHavePermission_returnsFalse() {
        // given
        String username = "testUserNoRead";
        
        // when
        boolean result = entityAclManager.hasReadPermission(testEntityId, testEntityClass, username);
        
        // then
        assertThat(result).isFalse();
    }
    
    // 테스트용 내부 클래스
    private static class TestEntity {
        private final Long id;
        private final String name;
        
        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Long getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
    }
}