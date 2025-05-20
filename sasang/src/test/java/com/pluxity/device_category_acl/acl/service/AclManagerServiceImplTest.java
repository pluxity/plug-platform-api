package com.pluxity.device_category_acl.acl.service;

import com.pluxity.SasangApplication;
import com.pluxity.domains.acl.service.AclManagerServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(classes = SasangApplication.class)
@Transactional
@ActiveProfiles("test")
class AclManagerServiceImplTest {

    @Autowired
    private AclManagerServiceImpl aclManagerService;
    
    @Autowired
    private MutableAclService mutableAclService; 

    @Autowired
    private AclCache aclCache;

    private ObjectIdentity objectIdentity;
    private Sid userSid;
    private Sid roleSid;
    private Long objectIdCounter = 1L;
    private Long objectId;
    private Class<TestDomainObject> domainType;

    private static class TestDomainObject implements Serializable {
        private final Serializable id;
        public TestDomainObject(Serializable id) { this.id = id; }
        public Serializable getId() { return id; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestDomainObject that = (TestDomainObject) o;
            return id.equals(that.id);
        }
        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    @BeforeEach
    void setUp() {
        objectId = objectIdCounter++;
        domainType = TestDomainObject.class;
        objectIdentity = new ObjectIdentityImpl(domainType, objectId);
        userSid = new PrincipalSid("testUser");
        roleSid = new GrantedAuthoritySid("ROLE_TEST");
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "adminUser", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        if (objectIdentity != null) {
            try {
                mutableAclService.deleteAcl(objectIdentity, false);
            } catch (NotFoundException e) {
                // 무시
            }
        }
        if (aclCache != null && objectIdentity != null) {
            aclCache.evictFromCache(objectIdentity);
        }
        SecurityContextHolder.clearContext();
    }
    
    @Nested
    @DisplayName("입력값 검증 테스트")
    class ValidationTests {
        
        @Test
        @DisplayName("도메인 타입 null 검증")
        void validateParams_nullDomainType_throwsException() {
            // given
            Class<?> nullDomainType = null;
            
            // when & then
            assertThatThrownBy(() -> 
                aclManagerService.addPermission(nullDomainType, objectId, userSid, BasePermission.READ)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Domain type cannot be null");
        }
        
        @Test
        @DisplayName("식별자 null 검증")
        void validateParams_nullIdentifier_throwsException() {
            // given
            Long nullIdentifier = null;
            
            // when & then
            assertThatThrownBy(() -> 
                aclManagerService.addPermission(domainType, nullIdentifier, userSid, BasePermission.READ)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Identifier cannot be null");
        }
        
        @Test
        @DisplayName("Sid null 검증")
        void validateSidAndPermission_nullSid_throwsException() {
            // given
            Sid nullSid = null;
            
            // when & then
            assertThatThrownBy(() -> 
                aclManagerService.addPermission(domainType, objectId, nullSid, BasePermission.READ)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sid cannot be null");
        }
        
        @Test
        @DisplayName("Permission null 검증")
        void validateSidAndPermission_nullPermission_throwsException() {
            // given
            Permission nullPermission = null;
            
            // when & then
            assertThatThrownBy(() -> 
                aclManagerService.addPermission(domainType, objectId, userSid, nullPermission)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permission cannot be null");
        }
        
        @ParameterizedTest
        @NullSource
        @EmptySource
        @DisplayName("사용자명 빈값 검증")
        void validateUsername_emptyUsername_throwsException(String username) {
            // given
            // username parameter from test

            // when & then
            assertThatThrownBy(() -> 
                aclManagerService.addPermissionForUser(domainType, objectId, username, BasePermission.READ)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username cannot be empty");
        }
        
        @ParameterizedTest
        @NullSource
        @EmptySource
        @DisplayName("역할명 빈값 검증")
        void validateRole_emptyRole_throwsException(String role) {
            // given
            // role parameter from test

            // when & then
            assertThatThrownBy(() -> 
                aclManagerService.addPermissionForRole(domainType, objectId, role, BasePermission.READ)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Role cannot be empty");
        }
        
        @Test
        @DisplayName("권한 목록 null 검증")
        void validatePermissions_nullPermissions_throwsException() {
            // given
            List<Permission> nullPermissions = null;
            
            // when & then
            assertThatThrownBy(() -> 
                aclManagerService.addPermissions(domainType, objectId, userSid, nullPermissions)
            )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Permissions list cannot be null");
        }
    }
    
    @Nested
    @DisplayName("ACL 생성 및 관리 테스트")
    class AclManagementTests {
        
        @Test
        @DisplayName("존재하지 않는 ACL에 대해 getOrCreateAcl이 새 ACL 생성")
        void getOrCreateAcl_nonExistingAcl_createsNewAcl() {
            // given
            Long newId = 99999L;
            ObjectIdentity newOi = new ObjectIdentityImpl(domainType, newId);
            
            try {
                // when & then
                assertThatThrownBy(() -> mutableAclService.readAclById(newOi))
                    .isInstanceOf(NotFoundException.class);
                
                // when
                aclManagerService.addPermissionForUser(domainType, newId, "testUser", BasePermission.READ);
                
                // then
                MutableAcl acl = (MutableAcl) mutableAclService.readAclById(newOi);
                assertThat(acl).isNotNull();
                assertThat(acl.getEntries()).hasSize(1);
            } finally {
                try {
                    mutableAclService.deleteAcl(newOi, false);
                } catch (NotFoundException e) {
                    // 무시
                }
            }
        }
        
        @Test
        @DisplayName("이미 존재하는 ACL에 대해 getOrCreateAcl이 기존 ACL 반환")
        void getOrCreateAcl_existingAcl_returnsExistingAcl() {
            // given
            aclManagerService.addPermissionForUser(domainType, objectId, "testUser", BasePermission.READ);
            
            // when
            aclManagerService.addPermissionForUser(domainType, objectId, "testUser", BasePermission.WRITE);
            
            // then
            MutableAcl acl = (MutableAcl) mutableAclService.readAclById(objectIdentity);
            assertThat(acl.getEntries()).hasSize(2);
        }
        
        @Test
        @DisplayName("findAndRemoveAce가 정확한 권한 항목 삭제")
        void findAndRemoveAce_removesCorrectEntry() {
            // given
            aclManagerService.addPermissionForUser(domainType, objectId, "testUser", BasePermission.READ);
            aclManagerService.addPermissionForUser(domainType, objectId, "testUser", BasePermission.WRITE);
            aclManagerService.addPermissionForUser(domainType, objectId, "otherUser", BasePermission.READ);
            
            // when
            aclManagerService.removePermissionForUser(domainType, objectId, "testUser", BasePermission.READ);
            
            // then
            assertThat(aclManagerService.hasPermissionForUser(domainType, objectId, "testUser", BasePermission.READ))
                .isFalse();
            assertThat(aclManagerService.hasPermissionForUser(domainType, objectId, "testUser", BasePermission.WRITE))
                .isTrue();
            assertThat(aclManagerService.hasPermissionForUser(domainType, objectId, "otherUser", BasePermission.READ))
                .isTrue();
        }
    }
    
    @Nested
    @DisplayName("다양한 Sid 유형 테스트")
    class SidTypeTests {
        
        @Test
        @DisplayName("PrincipalSid와 GrantedAuthoritySid가 동시에 작동")
        void differentSidTypes_workConcurrently() {
            // given
            aclManagerService.addPermissionForUser(domainType, objectId, "testUser", BasePermission.READ);
            aclManagerService.addPermissionForRole(domainType, objectId, "ROLE_TEST", BasePermission.WRITE);
            
            // when & then
            assertThat(aclManagerService.hasPermissionForUser(domainType, objectId, "testUser", BasePermission.READ))
                .isTrue();
            assertThat(aclManagerService.hasPermissionForRole(domainType, objectId, "ROLE_TEST", BasePermission.WRITE))
                .isTrue();
            
            // 교차 확인
            assertThat(aclManagerService.hasPermissionForRole(domainType, objectId, "ROLE_TEST", BasePermission.READ))
                .isFalse();
            assertThat(aclManagerService.hasPermissionForUser(domainType, objectId, "testUser", BasePermission.WRITE))
                .isFalse();
        }
    }

    @Nested
    @DisplayName("예외 처리 및 엣지 케이스 테스트")
    class ExceptionAndEdgeCaseTests {
        
        @Test
        @DisplayName("존재하지 않는 ACL에서 권한 제거 시도 시 예외 없이 처리")
        void removePermission_nonExistingAcl_handlesGracefully() {
            // given
            Long nonExistingId = 99999L;
            
            // when & then
            assertDoesNotThrow(() -> 
                aclManagerService.removePermissionForUser(domainType, nonExistingId, "testUser", BasePermission.READ)
            );
        }
        
        @Test
        @DisplayName("존재하지 않는 권한 제거 시도 시 예외 없이 처리")
        void removePermission_nonExistingPermission_handlesGracefully() {
            // given
            aclManagerService.addPermissionForUser(domainType, objectId, "testUser", BasePermission.READ);
            
            // when
            aclManagerService.removePermissionForUser(domainType, objectId, "testUser", BasePermission.WRITE);
            
            // then
            assertThat(aclManagerService.hasPermissionForUser(domainType, objectId, "testUser", BasePermission.READ))
                .isTrue();
        }
        
        @Test
        @DisplayName("getAclOptional이 존재하지 않는 ACL에 대해 빈 Optional 반환")
        void getAclOptional_nonExistingAcl_returnsEmptyOptional() {
            // given
            Long nonExistingId = 99999L;
            
            // when
            boolean result = aclManagerService.hasPermission(domainType, nonExistingId, userSid, BasePermission.READ);
            
            // then
            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("객체에 사용자 READ 권한 추가")
    void addPermissionForUser_integration() {
        // given
        String username = "testUser";
        
        // when
        aclManagerService.addPermissionForUser(domainType, objectId, username, BasePermission.READ);

        // then
        assertThat(aclManagerService.hasPermission(domainType, objectId, userSid, BasePermission.READ))
            .isTrue()
            .withFailMessage("READ permission should be granted after add");
    }
    
    @Test
    @DisplayName("객체에 사용자 여러 권한 추가")
    void addPermissionsForUser_integration() {
        // given
        String username = "testUser";
        List<Permission> permissions = List.of(BasePermission.READ, BasePermission.WRITE);
        
        // when
        aclManagerService.addPermissionsForUser(domainType, objectId, username, permissions);

        // then
        assertThat(aclManagerService.hasPermission(domainType, objectId, userSid, BasePermission.READ))
            .isTrue()
            .withFailMessage("READ permission should be granted after add");
        assertThat(aclManagerService.hasPermission(domainType, objectId, userSid, BasePermission.WRITE))
            .isTrue()
            .withFailMessage("WRITE permission should be granted after add");
    }

    @Test
    @DisplayName("객체에 역할 CREATE 권한 추가")
    void addPermissionForRole_integration() {
        // given
        String roleName = "ROLE_TEST";
        
        // when
        aclManagerService.addPermissionForRole(domainType, objectId, roleName, BasePermission.CREATE);

        // then
        assertThat(aclManagerService.hasPermission(domainType, objectId, roleSid, BasePermission.CREATE))
            .isTrue()
            .withFailMessage("CREATE permission should be granted for role after add");
    }

    @Test
    @DisplayName("객체에서 사용자 특정 권한 제거")
    void removePermissionForUser_integration() {
        // given
        String username = "testUser";
        aclManagerService.addPermissionForUser(domainType, objectId, username, BasePermission.READ);
        aclManagerService.addPermissionForUser(domainType, objectId, username, BasePermission.WRITE);

        // when
        aclManagerService.removePermissionForUser(domainType, objectId, username, BasePermission.READ);

        // then
        assertThat(aclManagerService.hasPermission(domainType, objectId, userSid, BasePermission.READ))
            .isFalse()
            .withFailMessage("READ permission should not be granted after removal for objectId: " + objectId);
        assertThat(aclManagerService.hasPermission(domainType, objectId, userSid, BasePermission.WRITE))
            .isTrue()
            .withFailMessage("WRITE permission should still be granted for objectId: " + objectId);
    }
    
    @Test
    @DisplayName("객체에서 사용자 특정 권한 목록 제거")
    void removePermissionsForUser_integration() {
        // given
        String username = "testUser";
        aclManagerService.addPermissionForUser(domainType, objectId, username, BasePermission.READ);
        aclManagerService.addPermissionForUser(domainType, objectId, username, BasePermission.WRITE);
        aclManagerService.addPermissionForUser(domainType, objectId, username, BasePermission.CREATE);

        List<Permission> permissionsToRemove = List.of(BasePermission.READ, BasePermission.WRITE);
        
        // when
        aclManagerService.removePermissionsForUser(domainType, objectId, username, permissionsToRemove);

        // then
        assertThat(aclManagerService.hasPermission(domainType, objectId, userSid, BasePermission.READ))
            .isFalse()
            .withFailMessage("READ permission should not be granted after removal for objectId: " + objectId);
        assertThat(aclManagerService.hasPermission(domainType, objectId, userSid, BasePermission.WRITE))
            .isFalse()
            .withFailMessage("WRITE permission should not be granted after removal for objectId: " + objectId);
        assertThat(aclManagerService.hasPermission(domainType, objectId, userSid, BasePermission.CREATE))
            .isTrue()
            .withFailMessage("CREATE permission should still be granted for objectId: " + objectId);
    }

    @Test
    @DisplayName("객체의 모든 권한 제거")
    void removeAllPermissions_integration() {
        // given
        aclManagerService.addPermissionForUser(domainType, objectId, "testUser", BasePermission.READ);
        
        // when
        aclManagerService.removeAllPermissions(domainType, objectId);

        // then
        assertThatThrownBy(() -> mutableAclService.readAclById(objectIdentity))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("사용자 권한 확인 - 권한 있음")
    void hasPermissionForUser_granted_integration() {
        // given
        String username = "testUser";
        aclManagerService.addPermissionForUser(domainType, objectId, username, BasePermission.READ);
        
        // when
        boolean hasPermission = aclManagerService.hasPermissionForUser(domainType, objectId, username, BasePermission.READ);
        
        // then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("역할 권한 확인 - 권한 없음 (ACL 없음)")
    void hasPermissionForRole_notFound_returnsFalse_integration() {
        // given
        String roleName = "ROLE_TEST";
        
        // when
        boolean hasPermission = aclManagerService.hasPermissionForRole(domainType, objectId, roleName, BasePermission.READ);
        
        // then
        assertThat(hasPermission).isFalse();
    }
    
    @Test
    @DisplayName("존재하지 않는 객체에 대한 권한 확인 시 false 반환")
    void hasPermissionForNonExistingObject_returnsFalse() {
        // given
        Long nonExistingObjectId = 99999L;
        String username = "testUser";
        
        // when
        boolean hasPermission = aclManagerService.hasPermissionForUser(domainType, nonExistingObjectId, username, BasePermission.READ);
        
        // then
        assertThat(hasPermission).isFalse();
    }
} 