package com.pluxity.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.pluxity.global.exception.CustomException;
import com.pluxity.permission.PermissionGroupRepository;
import com.pluxity.permission.PermissionGroupService;
import com.pluxity.permission.PermissionRepository;
import com.pluxity.permission.ResourceType;
import com.pluxity.permission.dto.PermissionGroupCreateRequest;
import com.pluxity.permission.dto.PermissionRequest;
import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.*;
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

    @Autowired
    private PermissionGroupService
            permissionGroupService; // PermissionService -> PermissionGroupService

    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private RolePermissionRepository rolePermissionRepository;
    @Autowired private PermissionGroupRepository permissionGroupRepository; // 추가
    @Autowired private PermissionRepository permissionRepository; // 추가

    @Autowired private EntityManager em;

    // 테스트 전체에서 사용할 고정된 ID
    private Long adminUserId, operatorUserId;
    private Long adminRoleId, operatorRoleId, viewerRoleId;
    // Permission ID -> PermissionGroup ID
    private Long userManageGroupId, facilityReadGroupId, facilityEditGroupId;

    /** 각 테스트 실행 전, 복잡하게 얽힌 상태를 미리 설정합니다. (PermissionGroup 중심 구조로 변경) */
    @BeforeEach
    void setUp() {
        // 모든 테이블 초기화 (참조 무결성 순서 고려)
        userRoleRepository.deleteAllInBatch();
        rolePermissionRepository.deleteAllInBatch();
        permissionRepository.deleteAllInBatch();
        permissionGroupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();

        em.flush();
        em.clear();

        // 1. PermissionGroup 생성
        userManageGroupId =
                permissionGroupService.create(
                        new PermissionGroupCreateRequest(
                                "사용자 관리 그룹",
                                "모든 사용자 관리 권한",
                                List.of(new PermissionRequest(ResourceType.FACILITY.name(), List.of("*")))));
        facilityReadGroupId =
                permissionGroupService.create(
                        new PermissionGroupCreateRequest(
                                "시설 조회 그룹",
                                "시설 조회 권한",
                                List.of(new PermissionRequest(ResourceType.FACILITY.name(), List.of("READ")))));
        facilityEditGroupId =
                permissionGroupService.create(
                        new PermissionGroupCreateRequest(
                                "시설 수정 그룹",
                                "시설 수정 권한",
                                List.of(new PermissionRequest(ResourceType.FACILITY.name(), List.of("EDIT")))));

        // 2. Role 생성 및 PermissionGroup 할당
        adminRoleId =
                roleService.save(
                        new RoleCreateRequest(
                                "ADMIN",
                                "관리자",
                                List.of(userManageGroupId, facilityReadGroupId, facilityEditGroupId)));
        operatorRoleId =
                roleService.save(
                        new RoleCreateRequest(
                                "OPERATOR", "운영자", List.of(facilityReadGroupId, facilityEditGroupId)));
        viewerRoleId =
                roleService.save(new RoleCreateRequest("VIEWER", "조회자", List.of(facilityReadGroupId)));

        // 3. User 생성 및 Role 할당
        adminUserId =
                userService
                        .save(
                                new UserCreateRequest(
                                        "admin", "pw", "Admin User", null, null, null, List.of(adminRoleId)))
                        .id();
        operatorUserId =
                userService
                        .save(
                                new UserCreateRequest(
                                        "operator", "pw", "Operator User", null, null, null, List.of(operatorRoleId)))
                        .id();

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("[연쇄 삭제 검증 1] 특정 Role 삭제 시, 해당 Role을 가진 User는 유지되지만 UserRole 연결은 끊어져야 한다")
    void deleteRole_shouldOnlyRemoveRoleAndUserRoleLink_notUser() {
        // 이 테스트는 Permission 모델 변경과 직접적인 관련이 없으므로, GIVEN 블록을 명확히 재설정하여 그대로 테스트
        // GIVEN
        rolePermissionRepository.deleteAllInBatch();
        userRoleRepository.deleteAllInBatch();
        roleRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        em.flush();
        em.clear();

        Role roleToDelete = roleRepository.save(new Role("DELETABLE_ROLE", "곧 삭제될 역할"));
        Role roleToKeep = roleRepository.save(new Role("KEEPER_ROLE", "유지될 역할"));

        User userWithTwoRoles = new User(null, "multiRoleUser", "pw", "다중역할사용자", "", null, null);
        userWithTwoRoles.addRole(roleToDelete);
        userWithTwoRoles.addRole(roleToKeep);
        userRepository.save(userWithTwoRoles);

        User userWithOneRole = new User(null, "singleRoleUser", "pw", "단일역할사용자", "", null, null);
        userWithOneRole.addRole(roleToDelete);
        userRepository.save(userWithOneRole);

        em.flush();
        em.clear();
        assertThat(userRoleRepository.count()).isEqualTo(3);

        // WHEN
        roleService.delete(roleToDelete.getId());
        em.flush();
        em.clear();

        // THEN
        assertThat(roleRepository.findById(roleToDelete.getId())).isEmpty();
        assertThat(roleRepository.findById(roleToKeep.getId())).isPresent();
        assertThat(userRepository.count()).isEqualTo(2);
        assertThat(userRoleRepository.count()).isEqualTo(1);

        User survivingUser1 = userRepository.findByUsername("multiRoleUser").orElseThrow();
        User survivingUser2 = userRepository.findByUsername("singleRoleUser").orElseThrow();
        assertThat(survivingUser1.getRoles()).hasSize(1);
        assertThat(survivingUser1.getRoles().get(0).getId()).isEqualTo(roleToKeep.getId());
        assertThat(survivingUser2.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("[연쇄 삭제 검증 2] 특정 PermissionGroup 삭제 시, Role들은 유지되지만 RolePermission 연결은 끊어져야 한다")
    void deletePermissionGroup_shouldOnlyRemoveGroupAndRolePermissionLink_notRole() {
        // GIVEN
        long initialRoleCount = roleRepository.count();
        long initialRolePermissionCount =
                rolePermissionRepository.count(); // ADMIN=3, OPERATOR=2, VIEWER=1 -> 6
        Role adminRoleBeforeDelete = roleRepository.findById(adminRoleId).get();
        Role operatorRoleBeforeDelete = roleRepository.findById(operatorRoleId).get();
        assertThat(adminRoleBeforeDelete.getRolePermissions()).hasSize(3);
        assertThat(operatorRoleBeforeDelete.getRolePermissions()).hasSize(2);

        // WHEN: 운영자와 관리자 모두 가진 '시설 수정 그룹' 삭제
        permissionGroupService.delete(facilityEditGroupId);
        em.flush();
        em.clear();

        // THEN
        assertThrows(CustomException.class, () -> permissionGroupService.findById(facilityEditGroupId));
        assertThat(roleRepository.count()).isEqualTo(initialRoleCount);
        assertThat(rolePermissionRepository.count()).isEqualTo(initialRolePermissionCount - 2);

        Role adminRoleAfterDelete = roleRepository.findById(adminRoleId).get();
        Role operatorRoleAfterDelete = roleRepository.findById(operatorRoleId).get();
        assertThat(adminRoleAfterDelete.getRolePermissions()).hasSize(2);
        assertThat(operatorRoleAfterDelete.getRolePermissions()).hasSize(1);
    }

    @Test
    @DisplayName("[연쇄 삭제 검증 3] User 삭제 시, User와 UserRole만 삭제되고 Role 자체는 유지되어야 한다")
    void deleteUser_shouldOnlyRemoveUserAndUserRoleLink_notRole() {
        // 이 테스트는 Permission 모델 변경과 관련 없으므로 그대로 유효
        // GIVEN
        long initialRoleCount = roleRepository.count();
        long initialUserRoleCount = userRoleRepository.count();

        // WHEN
        userService.delete(operatorUserId);
        em.flush();
        em.clear();

        // THEN
        assertThrows(EntityNotFoundException.class, () -> userService.findById(operatorUserId));
        assertThat(userRoleRepository.count()).isEqualTo(initialUserRoleCount - 1);
        assertThat(roleRepository.count()).isEqualTo(initialRoleCount);
    }

    @Test
    @DisplayName("[복합 업데이트 1] User의 Role을 완전히 다른 것으로 교체 (OPERATOR -> VIEWER)")
    void updateUserRole_fromOneToAnother() {
        // 이 테스트는 Permission 모델 변경과 관련 없으므로 그대로 유효
        // GIVEN
        User user = userRepository.findWithGraphById(operatorUserId).get();
        assertThat(user.getRoles().get(0).getId()).isEqualTo(operatorRoleId);

        // WHEN
        UserUpdateRequest request =
                new UserUpdateRequest(null, null, null, null, List.of(viewerRoleId));
        userService.update(operatorUserId, request);
        em.flush();
        em.clear();

        // THEN
        User updatedUser = userRepository.findWithGraphById(operatorUserId).get();
        assertThat(updatedUser.getRoles()).hasSize(1);
        assertThat(updatedUser.getRoles().get(0).getId()).isEqualTo(viewerRoleId);
    }

    @Test
    @DisplayName("[복합 업데이트 2] Role의 PermissionGroup 목록을 변경하면 User의 접근 권한이 즉시 변경되어야 한다")
    void updateRolePermissions_shouldReflectOnAllUsersWithThatRole() {
        // GIVEN
        User operator = userRepository.findWithGraphById(operatorUserId).get();
        // canAccess 메서드를 사용하여 권한 확인
        assertTrue(operator.canAccess("FACILITY", "EDIT"));

        // WHEN: OPERATOR 역할에서 '시설 수정 그룹'을 제거하고 '사용자 관리 그룹'을 추가
        roleService.update(
                operatorRoleId,
                new RoleUpdateRequest(
                        "운영자", "권한 변경된 운영자", List.of(facilityReadGroupId, userManageGroupId)));
        em.flush();
        em.clear();

        // THEN
        User updatedOperator = userRepository.findWithGraphById(operatorUserId).get();
        assertFalse(updatedOperator.canAccess("FACILITY", "EDIT")); // 수정 권한 없어짐
        assertTrue(updatedOperator.canAccess("FACILITY", "READ")); // 조회 권한 유지
        // userManageGroupId는 FACILITY ResourceType에 대해 '*' 권한을 가지므로, 아래와 같이 검증
        assertTrue(updatedOperator.canAccess("FACILITY", "*")); // 사용자 관리 권한 생김
    }

    @Test
    @DisplayName(
            "[전체 시나리오] PermissionGroup 삭제 -> User 역할 변경 -> Role 삭제 -> User 삭제 순으로 실행해도 데이터 정합성이 깨지지 않는다")
    void fullScenario_deletePermissionThenUpdateRoleThenUpdateUserThenDeleteUser() {
        // === 1. PermissionGroup 삭제 (facility_edit) ===
        permissionGroupService.delete(facilityEditGroupId);
        em.flush();
        em.clear();

        // THEN 1
        Role operatorRole1 = roleRepository.findById(operatorRoleId).get();
        assertThat(operatorRole1.getRolePermissions()).hasSize(1);
        User operator1 = userRepository.findWithGraphById(operatorUserId).get();
        // canAccess 메서드를 사용하여 권한 확인
        assertFalse(operator1.canAccess("FACILITY", "EDIT"));

        // === 2. User의 Role 변경 (operator -> viewer) ===
        userService.update(
                operatorUserId, new UserUpdateRequest(null, null, null, null, List.of(viewerRoleId)));
        em.flush();
        em.clear();

        // THEN 2
        User operator2 = userRepository.findWithGraphById(operatorUserId).get();
        assertThat(operator2.getRoles().get(0).getName()).isEqualTo("VIEWER");
        // canAccess 메서드를 사용하여 권한 확인
        assertFalse(operator2.canAccess("FACILITY", "EDIT"));
        assertTrue(operator2.canAccess("FACILITY", "READ"));

        // === 3. Role 삭제 (이제 아무도 쓰지 않는 OPERATOR Role) ===
        roleService.delete(operatorRoleId);
        em.flush();
        em.clear();

        // THEN 3
        assertThrows(EntityNotFoundException.class, () -> roleService.findById(operatorRoleId));
        assertThat(roleRepository.count()).isEqualTo(2);

        // === 4. User 삭제 (operator) ===
        userService.delete(operatorUserId);
        em.flush();
        em.clear();

        // THEN 4
        assertThrows(EntityNotFoundException.class, () -> userService.findById(operatorUserId));
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRoleRepository.count()).isEqualTo(1);

        // FINAL: admin 유저와 관련 데이터는 모두 온전해야 함
        assertThat(userRepository.findWithGraphById(adminUserId)).isPresent();
        assertThat(roleRepository.findById(adminRoleId)).isPresent();
        assertThat(permissionGroupRepository.findById(userManageGroupId)).isPresent();
    }
}
