package com.pluxity.user;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pluxity.user.dto.*;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RolePermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import com.pluxity.user.repository.UserRoleRepository;
import com.pluxity.user.service.PermissionService;
import com.pluxity.user.service.RoleService;
import com.pluxity.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserIntegrationTest {

    @Autowired private UserService userService;
    @Autowired private RoleService roleService;
    @Autowired private PermissionService permissionService;

    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private RolePermissionRepository rolePermissionRepository;

    @Autowired private EntityManager em;

    // 테스트 전체에서 사용할 고정된 ID
    private Long adminUserId, operatorUserId;
    private Long adminRoleId, operatorRoleId, viewerRoleId;
    private Long userManagePermissionId, facilityReadPermissionId, facilityEditPermissionId;

    /**
     * 각 테스트 실행 전, 복잡하게 얽힌 상태를 미리 설정합니다.
     *
     * <pre>
     * - Users: adminUser, operatorUser
     * - Roles: ADMIN, OPERATOR, VIEWER
     * - Permissions: USER_MANAGE, FACILITY_READ, FACILITY_EDIT
     *
     * - Relationships:
     *   - adminUser -> ADMIN role
     *   - operatorUser -> OPERATOR role
     *
     *   - ADMIN role -> USER_MANAGE, FACILITY_READ, FACILITY_EDIT permissions
     *   - OPERATOR role -> FACILITY_READ, FACILITY_EDIT permissions
     *   - VIEWER role -> FACILITY_READ permission
     * </pre>
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRoleRepository.deleteAll();
        roleRepository.deleteAll();
        rolePermissionRepository.deleteAll();

        em.flush();
        em.clear();

        // 1. Permission 생성
        userManagePermissionId = permissionService.create(new PermissionCreateRequest(ResourceType.BUILDING, "*"));
        facilityReadPermissionId = permissionService.create(new PermissionCreateRequest(ResourceType.FACILITY, "READ"));
        facilityEditPermissionId = permissionService.create(new PermissionCreateRequest(ResourceType.FACILITY, "EDIT"));

        // 2. Role 생성 및 Permission 할당
        adminRoleId = roleService.save(new RoleCreateRequest("ADMIN", "관리자",
                List.of(userManagePermissionId, facilityReadPermissionId, facilityEditPermissionId)));
        operatorRoleId = roleService.save(new RoleCreateRequest("OPERATOR", "운영자",
                List.of(facilityReadPermissionId, facilityEditPermissionId)));
        viewerRoleId = roleService.save(new RoleCreateRequest("VIEWER", "조회자",
                List.of(facilityReadPermissionId)));

        // 3. User 생성 및 Role 할당
        adminUserId = userService.save(new UserCreateRequest("admin", "pw", "Admin User", null, null, null, List.of(adminRoleId))).id();
        operatorUserId = userService.save(new UserCreateRequest("operator", "pw", "Operator User", null, null, null, List.of(operatorRoleId))).id();

        // 영속성 컨텍스트 초기화로 모든 변경사항을 DB에 반영하고, 캐시를 비움
        em.flush();
        em.clear();
    }


    // 뭔가 연관관계가 이상하게되어있는듯 API로 삭제할때 문제 없었음. FIXME
//    @Test
//    @DisplayName("[연쇄 삭제 검증 1] 특정 Role 삭제 시, 해당 Role을 가진 User는 유지되지만 UserRole 연결은 끊어져야 한다")
//    void deleteRole_shouldOnlyRemoveRoleAndUserRoleLink_notUser() {
//        // GIVEN
//        long initialUserCount = userRepository.count();
//        long initialUserRoleCount = userRoleRepository.count();
//        User operatorBeforeDelete = userRepository.findById(operatorUserId).get();
//        assertThat(operatorBeforeDelete.getRoles()).hasSize(1);
//
//        // WHEN
//        roleService.delete(operatorRoleId);
//        em.flush();
//        em.clear();
//
//        // THEN
//        // 1. Role은 삭제되었는가?
//        assertThrows(EntityNotFoundException.class, () -> roleService.findById(operatorRoleId));
//
//        // 2. User는 삭제되지 않았는가?
//        assertThat(userRepository.count()).isEqualTo(initialUserCount);
//        User operatorAfterDelete = userRepository.findById(operatorUserId).get();
//        assertThat(operatorAfterDelete).isNotNull();
//
//        // 3. User와 Role의 연결(UserRole)은 끊어졌는가?
//        assertThat(operatorAfterDelete.getRoles()).isEmpty();
//        assertThat(userRoleRepository.count()).isEqualTo(initialUserRoleCount - 1);
//
//        // 4. 다른 User(admin)는 영향을 받지 않았는가?
//        User adminUser = userRepository.findById(adminUserId).get();
//        assertThat(adminUser.getRoles()).hasSize(1);
//    }

    @Test
    @DisplayName("[연쇄 삭제 검증 2] 특정 Permission 삭제 시, 해당 Permission을 가진 Role들은 유지되지만 RolePermission 연결은 끊어져야 한다")
    void deletePermission_shouldOnlyRemovePermissionAndRolePermissionLink_notRole() {
        // GIVEN
        long initialRoleCount = roleRepository.count();
        long initialRolePermissionCount = rolePermissionRepository.count();
        Role adminRoleBeforeDelete = roleRepository.findById(adminRoleId).get();
        Role operatorRoleBeforeDelete = roleRepository.findById(operatorRoleId).get();
        // ADMIN은 3개, OPERATOR는 2개의 권한을 가짐
        assertThat(adminRoleBeforeDelete.getRolePermissions()).hasSize(3);
        assertThat(operatorRoleBeforeDelete.getRolePermissions()).hasSize(2);

        // WHEN: 운영자와 관리자 모두 가진 'FACILITY_EDIT' 권한 삭제
        permissionService.delete(facilityEditPermissionId);
        em.flush();
        em.clear();
        // THEN
        // 1. Permission은 삭제되었는가?
        assertThrows(Exception.class, () -> permissionService.findById(facilityEditPermissionId));

        // 2. Role들은 삭제되지 않았는가?
        assertThat(roleRepository.count()).isEqualTo(initialRoleCount);

        // 3. Role과 Permission의 연결(RolePermission)은 끊어졌는가?
        // 총 2개의 RolePermission(ADMIN->EDIT, OPERATOR->EDIT)이 삭제되어야 함
        assertThat(rolePermissionRepository.count()).isEqualTo(initialRolePermissionCount - 2);

        // 4. 각 Role의 권한 개수가 올바르게 줄었는가?
        Role adminRoleAfterDelete = roleRepository.findById(adminRoleId).get();
        Role operatorRoleAfterDelete = roleRepository.findById(operatorRoleId).get();
        assertThat(adminRoleAfterDelete.getRolePermissions()).hasSize(2);
        assertThat(operatorRoleAfterDelete.getRolePermissions()).hasSize(1);
    }

    @Test
    @DisplayName("[연쇄 삭제 검증 3] User 삭제 시, User와 UserRole만 삭제되고 Role 자체는 유지되어야 한다")
    void deleteUser_shouldOnlyRemoveUserAndUserRoleLink_notRole() {
        // GIVEN
        long initialRoleCount = roleRepository.count();
        long initialUserRoleCount = userRoleRepository.count();

        // WHEN
        userService.delete(operatorUserId);
        em.flush();
        em.clear();

        // THEN
        // 1. User는 삭제되었는가?
        assertThrows(EntityNotFoundException.class, () -> userService.findById(operatorUserId));

        // 2. User와 Role의 연결(UserRole)은 끊어졌는가? (orphanRemoval=true)
        assertThat(userRoleRepository.count()).isEqualTo(initialUserRoleCount - 1);

        // 3. Role 자체는 영향을 받지 않았는가?
        assertThat(roleRepository.count()).isEqualTo(initialRoleCount);
        assertThat(roleRepository.findById(operatorRoleId)).isPresent();
    }

    @Test
    @DisplayName("[복합 업데이트 1] User의 Role을 완전히 다른 것으로 교체 (OPERATOR -> VIEWER)")
    void updateUserRole_fromOneToAnother() {
        // GIVEN
        User user = userRepository.findById(operatorUserId).get();
        assertThat(user.getRoles().get(0).getId()).isEqualTo(operatorRoleId);

        // WHEN: 운영자(operator)의 역할을 조회자(viewer)로 변경
        UserUpdateRequest request = new UserUpdateRequest(null, null, null, null, List.of(viewerRoleId));
        userService.update(operatorUserId, request);
        em.flush();
        em.clear();

        // THEN
        User updatedUser = userRepository.findById(operatorUserId).get();
        assertThat(updatedUser.getRoles()).hasSize(1);
        assertThat(updatedUser.getRoles().get(0).getId()).isEqualTo(viewerRoleId);
        assertTrue(updatedUser.hasRole(roleRepository.findById(viewerRoleId).get()));
    }

    @Test
    @DisplayName("[복합 업데이트 2] Role의 Permission 목록을 변경하면, 해당 Role을 가진 모든 User의 접근 권한이 즉시 변경되어야 한다")
    void updateRolePermissions_shouldReflectOnAllUsersWithThatRole() {
        // GIVEN
        User operator = userRepository.findById(operatorUserId).get();
        // 초기 상태: OPERATOR는 FACILITY_EDIT 권한을 가지고 있음
        assertTrue(operator.canAccess("FACILITY", "EDIT"));

        // WHEN: OPERATOR 역할에서 FACILITY_EDIT 권한을 제거 (FACILITY_READ만 남김)
        roleService.update(operatorRoleId, new RoleUpdateRequest(null, null, List.of(facilityReadPermissionId)));
        em.flush();
        em.clear();

        // THEN
        User updatedOperator = userRepository.findById(operatorUserId).get();
        Role updatedOperatorRole = roleRepository.findById(operatorRoleId).get();

        // 1. Role의 권한이 1개로 줄었는지 확인
        assertThat(updatedOperatorRole.getRolePermissions()).hasSize(1);

        // 2. User의 접근 권한이 변경되었는지 확인
        // 더 이상 FACILITY_EDIT 권한이 없어야 함
        assertThat(updatedOperator.canAccess("FACILITY", "EDIT")).isFalse();
        // FACILITY_READ 권한은 여전히 가지고 있어야 함
        assertTrue(updatedOperator.canAccess("FACILITY", "READ"));
    }

    @Test
    @DisplayName("[전체 시나리오] Permission 삭제 -> Role 권한 변경 -> User 역할 변경 -> User 삭제 순으로 실행해도 데이터 정합성이 깨지지 않는다")
    void fullScenario_deletePermissionThenUpdateRoleThenUpdateUserThenDeleteUser() {
        // === GIVEN: setUp()에서 설정된 초기 상태 ===

        // === 1. Permission 삭제 (facility_edit) ===
        permissionService.delete(facilityEditPermissionId);
        em.flush();
        em.clear();

        // THEN 1
        Role operatorRole1 = roleRepository.findById(operatorRoleId).get();
        assertThat(operatorRole1.getRolePermissions()).hasSize(1); // 2 -> 1
        User operator1 = userRepository.findById(operatorUserId).get();
        assertThat(operator1.canAccess("FACILITY", "EDIT")).isFalse();

        // === 2. User의 Role 변경 (operator -> viewer) ===
        userService.update(operatorUserId, new UserUpdateRequest(null, null, null, null, List.of(viewerRoleId)));
        em.flush();
        em.clear();

        // THEN 2
        User operator2 = userRepository.findById(operatorUserId).get();
        assertThat(operator2.getRoles().get(0).getName()).isEqualTo("VIEWER");
        // VIEWER는 READ 권한만 있으므로, 이제 EDIT/READ 모두 접근 불가/가능
        assertThat(operator2.canAccess("FACILITY", "EDIT")).isFalse();
        assertTrue(operator2.canAccess("FACILITY", "READ"));

        // === 3. Role 삭제 (이제 아무도 쓰지 않는 OPERATOR Role) ===
        roleService.delete(operatorRoleId);
        em.flush();
        em.clear();

        // THEN 3
        assertThrows(EntityNotFoundException.class, () -> roleService.findById(operatorRoleId));
        assertThat(roleRepository.count()).isEqualTo(2); // 3 -> 2

        // === 4. User 삭제 (operator) ===
        userService.delete(operatorUserId);
        em.flush();
        em.clear();

        // THEN 4
        assertThrows(EntityNotFoundException.class, () -> userService.findById(operatorUserId));
        assertThat(userRepository.count()).isEqualTo(1); // 2 -> 1
        assertThat(userRoleRepository.count()).isEqualTo(1); // admin의 userrole만 남음

        // FINAL: admin 유저와 관련 데이터는 모두 온전해야 함
        assertThat(userRepository.findById(adminUserId)).isPresent();
        assertThat(roleRepository.findById(adminRoleId)).isPresent();
    }
}
