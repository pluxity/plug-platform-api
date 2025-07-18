package com.pluxity.user.service;

import com.pluxity.authentication.entity.RefreshToken;
import com.pluxity.authentication.repository.RefreshTokenRepository;
import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private User testUser;
    @Mock
    private User anotherUser;
    
    private Role testRole;
    private Role adminRole;
    
    @Mock
    private RefreshToken testUserRefreshToken;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 모킹
        lenient().when(testUser.getId()).thenReturn(1L);
        lenient().when(testUser.getUsername()).thenReturn("testuser");
        lenient().when(testUser.getPassword()).thenReturn("encodedPassword");
        lenient().when(testUser.getName()).thenReturn("테스트유저");
        lenient().when(testUser.getCode()).thenReturn("TEST001");
        lenient().when(testUser.getPhoneNumber()).thenReturn("010-1234-5678");
        lenient().when(testUser.getDepartment()).thenReturn("개발팀");
        lenient().when(testUser.getRoles()).thenReturn(new ArrayList<>());

        // 다른 사용자 모킹
        lenient().when(anotherUser.getId()).thenReturn(2L);
        lenient().when(anotherUser.getUsername()).thenReturn("anotheruser");
        lenient().when(anotherUser.getPassword()).thenReturn("encodedPassword2");
        lenient().when(anotherUser.getName()).thenReturn("다른유저");
        lenient().when(anotherUser.getCode()).thenReturn("ANO002");
        lenient().when(anotherUser.getPhoneNumber()).thenReturn("010-8765-4321");
        lenient().when(anotherUser.getDepartment()).thenReturn("기획팀");
        lenient().when(anotherUser.getRoles()).thenReturn(new ArrayList<>());

        // 테스트 역할 생성
        testRole = mock(Role.class);
        lenient().when(testRole.getId()).thenReturn(1L);
        lenient().when(testRole.getName()).thenReturn("ROLE_USER");
        
        // 관리자 역할 생성
        adminRole = mock(Role.class);
        lenient().when(adminRole.getId()).thenReturn(2L);
        lenient().when(adminRole.getName()).thenReturn("ROLE_ADMIN");
        
        // RefreshToken 모킹
        lenient().when(testUserRefreshToken.getToken()).thenReturn("some-refresh-token");
        lenient().when(testUserRefreshToken.getUsername()).thenReturn("testuser");
    }

    @Test
    @DisplayName("ID로 사용자 조회 - 성공")
    void findById_Success() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // when
        UserResponse response = userService.findById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.name()).isEqualTo("테스트유저");
        assertThat(response.code()).isEqualTo("TEST001");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("ID로 사용자 조회 - 실패 (사용자 없음)")
    void findById_UserNotFound() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with id: 1");
    }

//    @Test
//    @DisplayName("모든 사용자 조회")
//    void findAll() {
//        // given
//        List<User> users = new ArrayList<>();
//        users.add(testUser);
//        when(userRepository.findAll()).thenReturn(users);
//
//        // when
//        List<UserResponse> responses = userService.findAll();
//
//        // then
//        assertThat(responses).hasSize(1);
//        assertThat(responses.get(0).username()).isEqualTo("testuser");
//        verify(userRepository, times(1)).findAll();
//    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 성공")
    void findByUsername_Success() {
        // given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // when
        UserResponse response = userService.findByUsername("testuser");

        // then
        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("testuser");
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("사용자명으로 사용자 조회 - 실패 (사용자 없음)")
    void findByUsername_UserNotFound() {
        // given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with username: nonexistent");
    }

    @Test
    @DisplayName("사용자 생성 - 성공")
    void save_Success() {
        // given
        UserCreateRequest request = UserCreateRequest.builder()
                .username("newuser")
                .password("password123")
                .name("신규유저")
                .code("NEW001")
                .phoneNumber("010-9999-8888")
                .department("인사팀")
                .build();

        User newUser = mock(User.class);
        when(newUser.getId()).thenReturn(2L);
        when(newUser.getUsername()).thenReturn("newuser");
        when(newUser.getName()).thenReturn("신규유저");
        when(newUser.getCode()).thenReturn("NEW001");
        when(newUser.getPhoneNumber()).thenReturn("010-9999-8888");
        when(newUser.getDepartment()).thenReturn("인사팀");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // when
        UserResponse response = userService.save(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.name()).isEqualTo("신규유저");
        assertThat(response.code()).isEqualTo("NEW001");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

//    @Test
//    @DisplayName("사용자 정보 업데이트 - 성공")
//    void update_Success() {
//        // given
//        UserUpdateRequest request = UserUpdateRequest.builder()
//                .username("updateduser")
//                .name("업데이트유저")
//                .code("UPD001")
//                .phoneNumber("010-7777-6666")
//                .department("마케팅팀")
//                .build();
//
//        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
//
//        // 업데이트 후 값 모킹
//        when(testUser.getUsername()).thenReturn("updateduser");
//        when(testUser.getName()).thenReturn("업데이트유저");
//        when(testUser.getCode()).thenReturn("UPD001");
//        when(testUser.getPhoneNumber()).thenReturn("010-7777-6666");
//        when(testUser.getDepartment()).thenReturn("마케팅팀");
//
//        // when
//        UserResponse response = userService.update(1L, request);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.username()).isEqualTo("updateduser");
//        assertThat(response.name()).isEqualTo("업데이트유저");
//        assertThat(response.code()).isEqualTo("UPD001");
//        verify(userRepository, times(1)).findById(1L);
//        verify(testUser, times(1)).changeUsername("updateduser");
//        verify(testUser, times(1)).changeName("업데이트유저");
//        verify(testUser, times(1)).changeCode("UPD001");
//        verify(testUser, times(1)).changePhoneNumber("010-7777-6666");
//        verify(testUser, times(1)).changeDepartment("마케팅팀");
//    }

//    @Test
//    @DisplayName("사용자 정보 부분 업데이트 - 성공")
//    void update_PartialSuccess() {
//        // given
//        UserUpdateRequest request = UserUpdateRequest.builder()
//                .username("updateduser")
//                .phoneNumber("010-5555-4444")
//                .build();
//
//        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
//
//        // 업데이트 후 값 모킹
//        when(testUser.getUsername()).thenReturn("updateduser");
//
//        // when
//        UserResponse response = userService.update(1L, request);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.username()).isEqualTo("updateduser");
//        assertThat(response.name()).isEqualTo("테스트유저"); // 변경되지 않음
//        assertThat(response.code()).isEqualTo("TEST001"); // 변경되지 않음
//        verify(userRepository, times(1)).findById(1L);
//        verify(testUser, times(1)).changeUsername("updateduser");
//        verify(testUser, never()).changeName(anyString());
//        verify(testUser, never()).changeCode(anyString());
//    }

    @Test
    @DisplayName("사용자 삭제 - 성공")
    void delete_Success() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        // when
        userService.delete(1L);

        // then
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("사용자 비밀번호 업데이트 - 성공")
    void updateUserPassword_Success() {
        // given
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("password", "newPassword");

        // user.getPassword() → encodedPassword 로 반환됨
        when(testUser.getPassword()).thenReturn("encodedPassword");

        // 패스워드 매칭 성공
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        // 인코딩 후 newPassword → encodedNewPassword
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // findById → testUser 반환
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // changePassword 호출 시에는 상태를 바꾸는 게 아니므로, 검증만
        doNothing().when(testUser).changePassword("encodedNewPassword");

        // UserResponse.from(mock) 내부에서 필요한 getter 응답 설정
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getUsername()).thenReturn("testuser");
        when(testUser.getName()).thenReturn("테스트유저");
        when(testUser.getCode()).thenReturn("TEST001");

        // when
        userService.updateUserPassword(1L, request);

        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder).matches("password", "encodedPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(testUser).changePassword("encodedNewPassword");
    }

    @Test
    @DisplayName("사용자에게 역할 할당 - 성공")
    void assignRolesToUser_Success() {
        // given
        UserRoleAssignRequest request = new UserRoleAssignRequest(List.of(1L));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));

        // when
        UserResponse response = userService.assignRolesToUser(1L, request);

        // then
        assertThat(response).isNotNull();
        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findById(1L);
        verify(testUser, times(1)).addRoles(anyList());
    }

    @Test
    @DisplayName("사용자에게 여러 역할 할당 - 성공")
    void assignMultipleRolesToUser_Success() {
        // given
        UserRoleAssignRequest request = new UserRoleAssignRequest(List.of(1L, 2L));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(adminRole));

        // when
        UserResponse response = userService.assignRolesToUser(1L, request);

        // then
        assertThat(response).isNotNull();
        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findById(2L);
        verify(testUser, times(1)).addRoles(anyList());
    }

    @Test
    @DisplayName("사용자에서 역할 제거 - 성공")
    void removeRoleFromUser_Success() {
        // given
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(anyLong())).thenReturn(Optional.of(testRole));
        
        // when
        userService.removeRoleFromUser(1L, 1L);

        // then
        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findById(1L);
        verify(testUser, times(1)).removeRole(any(Role.class));
    }

    @Test
    @DisplayName("사용자 역할 업데이트 - 성공")
    void updateUserRoles_Success() {
        // given
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(List.of(1L));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(roleRepository.findAllById(anyList())).thenReturn(List.of(testRole));

        // when
        UserResponse response = userService.updateUserRoles(1L, request);

        // then
        assertThat(response).isNotNull();
        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findAllById(List.of(1L));
        verify(testUser, times(1)).updateRoles(anyList());
    }
    
    @Test
    @DisplayName("사용자 역할 업데이트 (여러 역할) - 성공")
    void updateUserMultipleRoles_Success() {
        // given
        UserRoleUpdateRequest request = new UserRoleUpdateRequest(List.of(1L, 2L));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(roleRepository.findAllById(anyList())).thenReturn(List.of(testRole, adminRole));

        // when
        UserResponse response = userService.updateUserRoles(1L, request);

        // then
        assertThat(response).isNotNull();
        verify(userRepository, times(1)).findById(1L);
        verify(roleRepository, times(1)).findAllById(List.of(1L, 2L));
        verify(testUser, times(1)).updateRoles(anyList());
    }
    
    @Test
    @DisplayName("사용자 역할 조회 - 성공")
    void getUserRoles_Success() {
        // given
        List<Role> roles = List.of(testRole);
        when(testUser.getRoles()).thenReturn(roles);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // when
        List<RoleResponse> roleResponses = userService.getUserRoles(1L);

        // then
        assertThat(roleResponses).isNotNull();
        assertThat(roleResponses).hasSize(1);
        verify(userRepository, times(1)).findById(1L);
        verify(testUser, times(1)).getRoles();
    }

    @Test
    @DisplayName("isLoggedIn - 모든 사용자 로그인 상태 조회 (로그인한 사용자, 로그아웃한 사용자 포함)")
    void isLoggedIn_ReturnsCorrectLoginStatusForAllUsers() {
        // given
        // testUser는 로그인 상태, anotherUser는 로그아웃 상태로 설정
        when(userRepository.findAll()).thenReturn(List.of(testUser, anotherUser));
        when(refreshTokenRepository.findById("testuser")).thenReturn(Optional.of(testUserRefreshToken)); // testuser는 토큰 있음 (로그인)
        when(refreshTokenRepository.findById("anotheruser")).thenReturn(Optional.empty());      // anotheruser는 토큰 없음 (로그아웃)

        // 역할 설정 (예시)
        List<Role> testUserRoles = List.of(testRole);
        when(testUser.getRoles()).thenReturn(testUserRoles);

        List<Role> anotherUserRoles = List.of(adminRole);
        when(anotherUser.getRoles()).thenReturn(anotherUserRoles);


        // when
        List<UserLoggedInResponse> responses = userService.isLoggedIn();

        // then
        assertThat(responses).hasSize(2);

        UserLoggedInResponse testUserResponse = responses.stream()
                .filter(r -> r.username().equals("testuser"))
                .findFirst()
                .orElseThrow();
        assertThat(testUserResponse.isLoggedIn()).isTrue();
        assertThat(testUserResponse.id()).isEqualTo(1L);
        assertThat(testUserResponse.name()).isEqualTo("테스트유저");
        assertThat(testUserResponse.code()).isEqualTo("TEST001");
        assertThat(testUserResponse.phoneNumber()).isEqualTo("010-1234-5678");
        assertThat(testUserResponse.department()).isEqualTo("개발팀");
        assertThat(testUserResponse.roles()).hasSize(1);
        assertThat(testUserResponse.roles().get(0).name()).isEqualTo("ROLE_USER");


        UserLoggedInResponse anotherUserResponse = responses.stream()
                .filter(r -> r.username().equals("anotheruser"))
                .findFirst()
                .orElseThrow();
        assertThat(anotherUserResponse.isLoggedIn()).isFalse();
        assertThat(anotherUserResponse.id()).isEqualTo(2L);
        assertThat(anotherUserResponse.name()).isEqualTo("다른유저");
        assertThat(anotherUserResponse.code()).isEqualTo("ANO002");
        assertThat(anotherUserResponse.phoneNumber()).isEqualTo("010-8765-4321");
        assertThat(anotherUserResponse.department()).isEqualTo("기획팀");
        assertThat(anotherUserResponse.roles()).hasSize(1);
        assertThat(anotherUserResponse.roles().get(0).name()).isEqualTo("ROLE_ADMIN");

        verify(userRepository, times(1)).findAll();
        verify(refreshTokenRepository, times(1)).findById("testuser");
        verify(refreshTokenRepository, times(1)).findById("anotheruser");
    }

    @Test
    @DisplayName("isLoggedIn - 모든 사용자가 로그인한 상태")
    void isLoggedIn_AllUsersLoggedIn() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(testUser, anotherUser));
        when(refreshTokenRepository.findById(anyString())).thenReturn(Optional.of(mock(RefreshToken.class))); // 모든 사용자가 토큰을 가짐

        // 역할 설정
        when(testUser.getRoles()).thenReturn(List.of(testRole));
        when(anotherUser.getRoles()).thenReturn(List.of(adminRole));

        // when
        List<UserLoggedInResponse> responses = userService.isLoggedIn();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.stream().allMatch(UserLoggedInResponse::isLoggedIn)).isTrue();
        verify(refreshTokenRepository, times(1)).findById("testuser");
        verify(refreshTokenRepository, times(1)).findById("anotheruser");
    }

    @Test
    @DisplayName("isLoggedIn - 모든 사용자가 로그아웃한 상태")
    void isLoggedIn_AllUsersLoggedOut() {
        // given
        when(userRepository.findAll()).thenReturn(List.of(testUser, anotherUser));
        when(refreshTokenRepository.findById(anyString())).thenReturn(Optional.empty()); // 모든 사용자가 토큰 없음

        // 역할 설정
        when(testUser.getRoles()).thenReturn(List.of(testRole));
        when(anotherUser.getRoles()).thenReturn(List.of(adminRole));

        // when
        List<UserLoggedInResponse> responses = userService.isLoggedIn();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.stream().noneMatch(UserLoggedInResponse::isLoggedIn)).isTrue();
        verify(refreshTokenRepository, times(1)).findById("testuser");
        verify(refreshTokenRepository, times(1)).findById("anotheruser");
    }

    @Test
    @DisplayName("isLoggedIn - 사용자가 없는 경우 빈 리스트 반환")
    void isLoggedIn_NoUsers() {
        // given
        when(userRepository.findAll()).thenReturn(List.of());

        // when
        List<UserLoggedInResponse> responses = userService.isLoggedIn();

        // then
        assertThat(responses).isEmpty();
        verify(refreshTokenRepository, never()).findById(anyString());
    }
} 