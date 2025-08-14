package com.pluxity.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pluxity.authentication.entity.RefreshToken;
import com.pluxity.authentication.repository.RefreshTokenRepository;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import com.pluxity.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RefreshTokenRepository refreshTokenRepository;

    // 테스트에서 공통으로 사용할 Role 엔티티
    private Role roleUser;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        // 모든 테스트 실행 전, 모든 데이터를 초기화하여 독립성 보장
        userRepository.deleteAll();
        roleRepository.deleteAll();
        refreshTokenRepository.deleteAll();

        // 공통으로 사용할 역할(Role) 생성
        roleUser = roleRepository.save(new Role("ROLE_USER", "description of role_user"));
        roleAdmin = roleRepository.save(new Role("ROLE_ADMIN", "description of role_admin"));
    }

    /** 테스트용 사용자를 생성하고 DB에 저장하는 헬퍼 메서드 */
    private User createUser(String username, String name, String code, List<Role> roles) {
        User user =
                User.builder()
                        .username(username)
                        .password(passwordEncoder.encode("password123"))
                        .name(name)
                        .code(code)
                        .department("테스트부서")
                        .phoneNumber("010-0000-0000")
                        .build();
        user.updateRoles(roles);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("성공: ID로 사용자 조회 시 정확한 사용자 정보를 반환한다")
    void findById_Success() {
        // given
        User savedUser = createUser("testuser", "테스트유저", "T001", List.of(roleUser));

        // when
        UserResponse response = userService.findById(savedUser.getId());

        // then
        assertThat(response.id()).isEqualTo(savedUser.getId());
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.name()).isEqualTo("테스트유저");
        assertThat(response.code()).isEqualTo("T001");
        assertThat(response.phoneNumber()).isEqualTo("010-0000-0000");
        assertThat(response.department()).isEqualTo("테스트부서");
        assertThat(response.shouldChangePassword()).isFalse();
        assertThat(response.roles()).hasSize(1);
        assertThat(response.roles().getFirst().name()).isEqualTo("ROLE_USER");
        assertThat(response.roles().getFirst().description()).isEqualTo("description of role_user");
        assertThat(response.roles().getFirst().permissions()).isEmpty();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 사용자 조회 시 예외가 발생한다")
    void findById_UserNotFound() {
        // when & then
        assertThatThrownBy(() -> userService.findById(9999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("성공: 사용자 생성 시 비밀번호는 암호화되고 DB에 저장된다")
    void save_Success() {
        // given
        UserCreateRequest request =
                UserCreateRequest.builder()
                        .username("newuser")
                        .password("password123")
                        .name("신규유저")
                        .code("NEW001")
                        .build();

        // when
        UserResponse response = userService.save(request);

        // then
        assertThat(response.id()).isNotNull();
        User foundUser = userRepository.findById(response.id()).orElseThrow();

        assertThat(foundUser.getUsername()).isEqualTo("newuser");
        assertThat(passwordEncoder.matches("password123", foundUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("성공: 사용자 정보 업데이트 시 변경된 내용이 DB에 반영된다")
    void update_Success() {
        // given
        User savedUser = createUser("testuser", "원본이름", "ORI001", List.of());
        UserUpdateRequest request = UserUpdateRequest.builder().name("수정이름").department("수정부서").build();

        // when
        userService.update(savedUser.getId(), request);

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("수정이름");
        assertThat(updatedUser.getDepartment()).isEqualTo("수정부서");
        assertThat(updatedUser.getCode()).isEqualTo("ORI001"); // 변경되지 않은 필드는 유지
    }

    @Test
    @DisplayName("성공: 사용자 삭제 시 DB에서 해당 사용자가 삭제된다")
    void delete_Success() {
        // given
        User savedUser = createUser("deleteuser", "삭제유저", "DEL001", List.of());
        Long userId = savedUser.getId();
        assertThat(userRepository.existsById(userId)).isTrue();

        // when
        userService.delete(userId);

        // then
        assertThat(userRepository.existsById(userId)).isFalse();
    }

    @Test
    @DisplayName("성공: 사용자 비밀번호 업데이트 시 DB의 비밀번호가 변경된다")
    void updateUserPassword_Success() {
        // given
        User savedUser = createUser("testuser", "테스트유저", "T001", List.of());
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password123", "newPassword");

        // when
        userService.updateUserPassword(savedUser.getId(), request);

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword", updatedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("실패: 현재 비밀번호가 틀리면 비밀번호 업데이트 시 예외가 발생한다")
    void updateUserPassword_withWrongCurrentPassword_throwsException() {
        // given
        User savedUser = createUser("testuser", "테스트유저", "T001", List.of());
        UserPasswordUpdateRequest request =
                new UserPasswordUpdateRequest("wrongPassword", "newPassword");

        // when & then
        assertThatThrownBy(() -> userService.updateUserPassword(savedUser.getId(), request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("성공: 사용자에게 여러 역할을 할당하면 정상적으로 반영된다")
    void assignRolesToUser_Success() {
        // given
        User savedUser = createUser("testuser", "테스트유저", "T001", List.of());
        UserRoleAssignRequest request =
                new UserRoleAssignRequest(List.of(roleUser.getId(), roleAdmin.getId()));

        // when
        userService.assignRolesToUser(savedUser.getId(), request);

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getRoles()).hasSize(2);
        assertThat(updatedUser.getRoles())
                .extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("성공: 사용자 역할 업데이트 시 기존 역할은 지워지고 새 역할만 남는다")
    void updateUserRoles_Success() {
        // given
        User savedUser = createUser("testuser", "테스트유저", "T001", List.of(roleUser));
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(List.of(roleAdmin.getId()));

        // when
        userService.updateUserRoles(savedUser.getId(), request);

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getRoles()).hasSize(1);
        assertThat(updatedUser.getRoles().getFirst().getName()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("성공: isLoggedIn 호출 시 사용자의 로그인 상태를 정확히 반환한다")
    void isLoggedIn_ReturnsCorrectLoginStatus() {
        // given
        User loggedInUser = createUser("loggedIn", "로그인유저", "L001", List.of(roleUser));
        User loggedOutUser = createUser("loggedOut", "로그아웃유저", "O001", List.of(roleAdmin));

        // 로그인한 사용자의 리프레시 토큰 저장
        refreshTokenRepository.save(
                RefreshToken.of(loggedInUser.getUsername(), "some-token-value", 60));

        // when
        List<UserLoggedInResponse> responses = userService.isLoggedIn();

        // then
        assertThat(responses).hasSize(2);

        UserLoggedInResponse loggedInResponse =
                responses.stream().filter(r -> r.username().equals("loggedIn")).findFirst().orElseThrow();
        assertThat(loggedInResponse.isLoggedIn()).isTrue();
        assertThat(loggedInResponse.roles().getFirst().name()).isEqualTo("ROLE_USER");

        UserLoggedInResponse loggedOutResponse =
                responses.stream().filter(r -> r.username().equals("loggedOut")).findFirst().orElseThrow();
        assertThat(loggedOutResponse.isLoggedIn()).isFalse();
        assertThat(loggedOutResponse.roles().getFirst().name()).isEqualTo("ROLE_ADMIN");
    }
}
