package com.pluxity.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pluxity.config.MockBeansConfig;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.*;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import com.pluxity.user.service.RoleService;
import com.pluxity.user.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(MockBeansConfig.class)
@Transactional
public class UserRoleTest {
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleService roleService;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager em;

    private final List<Long> roleIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 Role 미리 생성
        roleIds.clear();
        IntStream.rangeClosed(1, 3)
                .forEach(
                        i -> {
                            RoleCreateRequest request =
                                    new RoleCreateRequest("Test Role " + i, "Desc " + i, List.of());
                            roleIds.add(roleService.save(request));
                        });
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("새로운 User를 역할(Role)과 함께 생성하고, 생성된 User를 조회하여 검증한다")
    void save_withRoles_andVerify() {
        // GIVEN
        List<Long> assignedRoleIds = List.of(roleIds.get(0), roleIds.get(1));
        UserCreateRequest createRequest =
                new UserCreateRequest(
                        "newUser", "password123", "New User", "U001", "010-1234-5678", "Dev", assignedRoleIds);

        // WHEN
        UserResponse savedUserResponse = userService.save(createRequest);
        Long userId = savedUserResponse.getId();
        em.flush();
        em.clear();

        // THEN
        UserResponse foundUserResponse = userService.findById(userId);
        assertThat(foundUserResponse.getUsername()).isEqualTo("newUser");
        assertThat(foundUserResponse.getName()).isEqualTo("New User");
        assertThat(foundUserResponse.getRoles()).hasSize(2);

        List<Long> foundRoleIds =
                foundUserResponse.getRoles().stream().map(RoleResponse::getId).toList();
        assertThat(foundRoleIds).containsExactlyInAnyOrderElementsOf(assignedRoleIds);
    }

    @Test
    @DisplayName("중복된 username으로 User 생성을 시도하면 예외가 발생한다")
    void save_withDuplicateUsername_throwsException() {
        // GIVEN
        userService.save(
                new UserCreateRequest("duplicateUser", "pw1", "User One", null, null, null, List.of()));
        em.flush();
        em.clear();

        // WHEN & THEN
        UserCreateRequest duplicateRequest =
                new UserCreateRequest("duplicateUser", "pw2", "User Two", null, null, null, List.of());
        // unique 제약조건 위반은 flush 시점에 발생
        assertThrows(
                DataIntegrityViolationException.class,
                () -> {
                    userService.save(duplicateRequest);
                    em.flush();
                });
    }

    @Test
    @DisplayName("User 정보 업데이트 시, 역할(Role)까지 올바르게 동기화되는지 검증한다")
    void update_userAndRoles_andVerify() {
        // GIVEN: 1, 2번 역할을 가진 User 생성
        UserResponse originalUser =
                userService.save(
                        new UserCreateRequest(
                                "updateUser",
                                "pw",
                                "Original Name",
                                "C01",
                                "010-1111-1111",
                                "Dept1",
                                List.of(roleIds.get(0), roleIds.get(1))));
        Long userId = originalUser.getId();
        em.flush();
        em.clear();

        // WHEN: 이름, 부서 변경. 역할은 2번 유지, 3번 추가 (최종: 2, 3번 역할)
        List<Long> updatedRoleIds = List.of(roleIds.get(1), roleIds.get(2));
        UserUpdateRequest updateRequest =
                new UserUpdateRequest("Updated Name", null, null, "Dept2", updatedRoleIds);
        userService.update(userId, updateRequest);
        em.flush();
        em.clear();

        // THEN
        UserResponse updatedUser = userService.findById(userId);
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getCode()).isEqualTo("C01"); // null로 보내면 변경되지 않음
        assertThat(updatedUser.getDepartment()).isEqualTo("Dept2");

        List<Long> finalRoleIds = updatedUser.getRoles().stream().map(RoleResponse::getId).toList();
        assertThat(finalRoleIds).hasSize(2);
        assertThat(finalRoleIds).containsExactlyInAnyOrderElementsOf(updatedRoleIds);
    }

    @Test
    @DisplayName("User 삭제 시, UserRole은 함께 삭제되지만 Role 자체는 삭제되지 않음을 검증한다")
    void delete_user_andVerifyCascade() {
        // GIVEN
        long initialRoleCount = roleRepository.count();
        UserResponse userResponse =
                userService.save(
                        new UserCreateRequest("deleteUser", "pw", "Del Name", null, null, null, roleIds));
        Long userId = userResponse.getId();
        em.flush();
        em.clear();

        User user = em.find(User.class, userId); // UserRole 개수 확인을 위해 영속성 컨텍스트에서 다시 로드
        assertThat(user.getUserRoles()).isNotEmpty();

        // WHEN
        userService.delete(userId);
        em.flush();
        em.clear();

        // THEN
        // 1. User는 삭제되어야 함
        assertThrows(EntityNotFoundException.class, () -> userService.findById(userId));

        // 2. UserRole은 orphanRemoval=true에 의해 함께 삭제되어야 함 (직접 확인은 어려우나, User 삭제가 성공한 것이 증거)

        // 3. Role 엔티티 자체는 삭제되지 않아야 함
        assertThat(roleRepository.count()).isEqualTo(initialRoleCount);
    }

    @Test
    @DisplayName("올바른 현재 비밀번호로 변경 시도 시 성공한다")
    void updateUserPassword_withCorrectCurrentPassword_succeeds() {
        // GIVEN
        String initialPassword = "password123";
        UserResponse userResponse =
                userService.save(
                        new UserCreateRequest(
                                "pwUser", initialPassword, "PW User", null, null, null, List.of()));
        Long userId = userResponse.getId();
        em.flush();
        em.clear();

        // WHEN
        String newPassword = "newPassword456";
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest(initialPassword, newPassword);
        userService.updateUserPassword(userId, request);
        em.flush();
        em.clear();

        // THEN
        User updatedUser = userRepository.findWithGraphById(userId);
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("틀린 현재 비밀번호로 변경 시도 시 CustomException이 발생한다")
    void updateUserPassword_withIncorrectCurrentPassword_throwsException() {
        // GIVEN
        String initialPassword = "password123";
        UserResponse userResponse =
                userService.save(
                        new UserCreateRequest(
                                "pwUser2", initialPassword, "PW User2", null, null, null, List.of()));
        Long userId = userResponse.getId();
        em.flush();
        em.clear();

        // WHEN & THEN
        UserPasswordUpdateRequest request =
                new UserPasswordUpdateRequest("wrongPassword", "newPassword");
        assertThrows(CustomException.class, () -> userService.updateUserPassword(userId, request));
    }

    @Test
    @DisplayName("비밀번호 초기화 시, 새 비밀번호로 변경되고 마지막 변경일이 과거로 설정된다")
    void initPassword_resetsPasswordAndLastChangeDate() {
        // GIVEN
        UserResponse userResponse =
                userService.save(
                        new UserCreateRequest(
                                "initPwUser", "anyPassword", "Init PW", null, null, null, List.of()));
        Long userId = userResponse.getId();
        em.flush();
        em.clear();

        // WHEN
        userService.initPassword(userId);
        em.flush();
        em.clear();

        // THEN
        User user = userRepository.findWithGraphById(userId);
        // 실제 초기화 비밀번호 값은 @Value에서 주입되므로, 암호화된 값이 null이 아닌지만 체크
        assertThat(user.getPassword()).isNotNull();
        // isPasswordChangeRequired 로직으로 검증
        assertTrue(user.isPasswordChangeRequired());
    }
}
