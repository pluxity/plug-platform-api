package com.pluxity.device_category_acl.acl.service;

import com.pluxity.SasangApplication;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.domains.acl.service.AclManagerService;
import com.pluxity.domains.acl.service.EntityAclManager;
import com.pluxity.domains.acl.device_category.dto.PermissionRequestDto;
import com.pluxity.domains.acl.device_category.dto.PermissionRequestDto.PermissionOperation;
import com.pluxity.domains.acl.device_category.dto.PermissionRequestDto.PermissionTarget;
import com.pluxity.user.dto.*;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.service.RoleService;
import com.pluxity.user.service.UserService;
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
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private EntityAclManager entityAclManager;
    private final Long testEntityId = 1L;
    private final String testEntityType = "DeviceCategory";
    private final Class<?> testEntityClass = DeviceCategory.class;
    private Authentication originalAuth;
    
    private Long testUserId;
    private String testUsername;
    private Long editorRoleId;
    private String editorRoleName;

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
        
        // 테스트용 사용자와 역할 생성
        testUsername = "testUser" + System.currentTimeMillis();
        UserResponse userResponse = userService.save(new UserCreateRequest(
            testUsername, "password123", "테스트 사용자", "TEST001"
        ));
        testUserId = userResponse.id();
        
        editorRoleName = "EDITOR" + System.currentTimeMillis();
        RoleResponse roleResponse = roleService.save(new RoleCreateRequest(
            editorRoleName, "편집자 역할"
        ));
        editorRoleId = roleResponse.id();
        
        // 사용자에게 역할 할당
        userService.assignRolesToUser(testUserId, new UserRoleAssignRequest(List.of(editorRoleId)));
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
        
        // 사용자와 역할 정리도 필요하지만 @Transactional로 롤백될 것이므로 생략
        
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
        List<String> permissionStrings = List.of("READ", "WRITE", "CREATE", "DELETE");

        // when
        List<Permission> result = entityAclManager.convertToPermissions(permissionStrings);

        // then
        assertThat(result).hasSize(4);
        assertThat(result).contains(BasePermission.READ, BasePermission.WRITE, BasePermission.CREATE, BasePermission.DELETE);
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
    @DisplayName("managePermission: 역할에게 권한 부여 시 권한이 정상적으로 추가됨")
    void managePermission_forRole_grantsPermissions() {
        // given
        String roleName = "ROLE_" + editorRoleName;
        PermissionRequestDto request = new PermissionRequestDto(
            testEntityType,
            roleName,
            List.of(new PermissionTarget(testEntityId, PermissionOperation.GRANT))
        );

        // when
        entityAclManager.managePermission(request, testEntityType, testEntityClass);

        // then
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ))
            .isTrue();
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.WRITE))
            .isTrue();
    }

    @Test
    @DisplayName("managePermission: 잘못된 엔티티 타입으로 권한 부여 시도 시 예외 발생")
    void managePermission_wrongEntityType_throwsException() {
        // given
        String roleName = "ROLE_" + editorRoleName;
        PermissionRequestDto request = new PermissionRequestDto(
            "WrongType", 
            roleName, 
            List.of(new PermissionTarget(testEntityId, PermissionOperation.GRANT))
        );
        
        // when & then
        assertThatThrownBy(() -> entityAclManager.managePermission(request, testEntityType, testEntityClass))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Target type must be");
    }
    
    @Test
    @DisplayName("managePermission: 역할의 모든 권한 회수 시 권한이 제거됨")
    void managePermission_removeAllForRole_revokesAllPermissions() {
        // given
        String roleName = "ROLE_" + editorRoleName;
        // 먼저 권한 부여
        aclManagerService.addPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ);
        aclManagerService.addPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.WRITE);
        
        PermissionRequestDto request = new PermissionRequestDto(
            testEntityType, 
            roleName, 
            List.of(new PermissionTarget(testEntityId, PermissionOperation.REVOKE))
        );
        
        // when
        entityAclManager.managePermission(request, testEntityType, testEntityClass);
        
        // then
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ))
            .isFalse();
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.WRITE))
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
        
        // entity1에만 접근 권한 부여
        String roleName = "ROLE_" + editorRoleName;
        
        // 먼저 이전 권한 정리 (ACL이 있다면)
        try {
            ObjectIdentity oi = new ObjectIdentityImpl(testEntityClass, 1L);
            mutableAclService.deleteAcl(oi, false);
        } catch (NotFoundException e) {
            // 무시
        }
        
        // 새로 권한 부여
        aclManagerService.addPermissionForRole(testEntityClass, 1L, roleName, BasePermission.READ);
        
        // 일반 사용자로 인증 설정 - 인증 정보에서 사용자 이름이 중요!
        // EntityAclManager.findAllAllowedForCurrentUser는 인증된 사용자의 이름(username)을 사용
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
            roleName, "password", List.of(new SimpleGrantedAuthority(roleName))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        
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
    @DisplayName("hasReadPermission: 역할이 읽기 권한 있는 경우 true 반환")
    void hasReadPermission_roleHasPermission_returnsTrue() {
        // given
        String roleName = "ROLE_" + editorRoleName;
        aclManagerService.addPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ);
        
        // when - roleAuthority로 테스트 (참고: EntityAclManager.hasReadPermission은 역할 이름으로 확인)
        boolean result = entityAclManager.hasReadPermission(testEntityId, testEntityClass, roleName);
        
        // then
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("hasReadPermission: 역할이 읽기 권한 없는 경우 false 반환")
    void hasReadPermission_roleDoesNotHavePermission_returnsFalse() {
        // given
        String roleName = "ROLE_VIEWER"; // 존재하지 않는 역할
        
        // when
        boolean result = entityAclManager.hasReadPermission(testEntityId, testEntityClass, roleName);
        
        // then
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("managePermission: 순차적 요청 - 같은 targetId에 대해 GRANT 후 REVOKE 작업 시 최종 상태는 REVOKE")
    void managePermission_sequentialRequests_finalStateIsRevoke() {
        // given
        String roleName = "ROLE_" + editorRoleName;
        
        // 1. 먼저 GRANT 작업 수행
        PermissionRequestDto grantRequest = new PermissionRequestDto(
            testEntityType, 
            roleName, 
            List.of(new PermissionTarget(testEntityId, PermissionOperation.GRANT))
        );
        
        entityAclManager.managePermission(grantRequest, testEntityType, testEntityClass);
        
        // GRANT 작업 후 권한이 있는지 확인
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ))
            .isTrue()
            .withFailMessage("After GRANT, READ permission should be granted");
        
        // 2. 이후 REVOKE 작업 수행
        PermissionRequestDto revokeRequest = new PermissionRequestDto(
            testEntityType, 
            roleName, 
            List.of(new PermissionTarget(testEntityId, PermissionOperation.REVOKE))
        );
        
        entityAclManager.managePermission(revokeRequest, testEntityType, testEntityClass);
        
        // then
        // REVOKE 작업 후 권한이 없는지 확인
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ))
            .isFalse()
            .withFailMessage("After REVOKE, READ permission should be revoked");
    }
    
    @Test
    @DisplayName("managePermission: 동일 요청 내 충돌 - 동일한 targetId에 대해 GRANT와 REVOKE 모두 있을 때 마지막 operation이 적용됨")
    void managePermission_conflictingOperations_lastOperationWins() {
        // given
        String roleName = "ROLE_" + editorRoleName;
        
        // GRANT와 REVOKE가 모두 포함된 요청 생성
        // 순서: 1. GRANT, 2. REVOKE (REVOKE가 나중에 처리되므로 최종 상태는 REVOKE여야 함)
        PermissionRequestDto conflictRequest = new PermissionRequestDto(
            testEntityType, 
            roleName, 
            List.of(
                new PermissionTarget(testEntityId, PermissionOperation.GRANT),
                new PermissionTarget(testEntityId, PermissionOperation.REVOKE)
            )
        );
        
        // when
        entityAclManager.managePermission(conflictRequest, testEntityType, testEntityClass);
        
        // then
        // 최종 상태는 REVOKE여야 함 (targets 배열에서 마지막에 처리된 operation)
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ))
            .isFalse()
            .withFailMessage("When both GRANT and REVOKE operations exist for the same targetId, the last one (REVOKE) should be effective");
    }
    
    @Test
    @DisplayName("managePermission: 동일 요청 내 충돌(반대 순서) - 동일한 targetId에 대해 REVOKE 후 GRANT가 있을 때 마지막 operation이 적용됨")
    void managePermission_conflictingOperationsReversed_lastOperationWins() {
        // given
        String roleName = "ROLE_" + editorRoleName;
        
        // REVOKE와 GRANT가 모두 포함된 요청 생성 (순서를 반대로)
        // 순서: 1. REVOKE, 2. GRANT (GRANT가 나중에 처리되므로 최종 상태는 GRANT여야 함)
        PermissionRequestDto conflictRequest = new PermissionRequestDto(
            testEntityType, 
            roleName, 
            List.of(
                new PermissionTarget(testEntityId, PermissionOperation.REVOKE),
                new PermissionTarget(testEntityId, PermissionOperation.GRANT)
            )
        );
        
        // when
        entityAclManager.managePermission(conflictRequest, testEntityType, testEntityClass);
        
        // then
        // 최종 상태는 GRANT여야 함 (targets 배열에서 마지막에 처리된 operation)
        assertThat(aclManagerService.hasPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ))
            .isTrue()
            .withFailMessage("When both REVOKE and GRANT operations exist for the same targetId, the last one (GRANT) should be effective");
    }
    
    @Test
    @DisplayName("UserService와 연동: 사용자에게 역할 부여 후 권한 획득 확인")
    void userService_assignRoleToUser_thenGrantPermission() {
        // given
        String roleName = "ROLE_" + editorRoleName;
        
        // 1. 역할에 권한 부여
        try {
            ObjectIdentity oi = new ObjectIdentityImpl(testEntityClass, testEntityId);
            mutableAclService.deleteAcl(oi, false);
        } catch (NotFoundException e) {
            // 무시
        }
        
        // 권한 부여
        aclManagerService.addPermissionForRole(testEntityClass, testEntityId, roleName, BasePermission.READ);
        
        // 2. 사용자 인증으로 변경 - 인증 정보에서 사용자 이름이 중요!
        // EntityAclManager의 findAllAllowedForCurrentUser는 인증된 사용자의 이름(username)을 사용
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
            roleName, "password", List.of(new SimpleGrantedAuthority(roleName))
        );
        SecurityContextHolder.getContext().setAuthentication(userAuth);
        
        // 3. 해당 엔티티에 대한 권한이 있는지 확인
        TestEntity entity = new TestEntity(testEntityId, "Test Entity");
        List<TestEntity> entities = List.of(entity);
        
        // when 
        List<String> result = entityAclManager.findAllAllowedForCurrentUser(
            entities, TestEntity::getId, TestEntity::getName, testEntityClass
        );
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result).contains("Test Entity");
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