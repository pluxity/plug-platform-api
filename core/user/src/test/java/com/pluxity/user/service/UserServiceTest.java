package com.pluxity.user.service;

import com.pluxity.user.dto.*;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.Template;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("모든 사용자를 조회할 수 있다")
    void findAll() {
        // given
        User user1 = User.builder()
                .username("username1")
                .password(passwordEncoder.encode("password1"))
                .name("name1")
                .code("code1")
                .build();

        User user2 = User.builder()
                .username("username2")
                .password(passwordEncoder.encode("password2"))
                .name("name2")
                .code("code2")
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // when
        List<UserResponse> users = userService.findAll();

        // then
        assertThat(users).hasSize(2);
        assertThat(users).extracting("username")
                .containsExactlyInAnyOrder("username1", "username2");
    }

    @Test
    @DisplayName("ID로 사용자를 조회할 수 있다")
    void findById() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();

        User savedUser = userRepository.save(user);

        // when
        UserResponse foundUser = userService.findById(savedUser.getId());

        // then
        assertThat(foundUser.id()).isEqualTo(savedUser.getId());
        assertThat(foundUser.username()).isEqualTo("username");
        assertThat(foundUser.name()).isEqualTo("name");
        assertThat(foundUser.code()).isEqualTo("code");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회시 예외가 발생한다")
    void findById_notFound() {
        // given
        Long notFoundId = 999L;

        // when & then
        assertThatThrownBy(() -> userService.findById(notFoundId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("새로운 사용자를 생성할 수 있다")
    void save() {
        // given
        UserCreateRequest request = UserCreateRequest.builder()
                .username("username")
                .password("password")
                .name("name")
                .code("code")
                .build();

        // when
        UserResponse result = userService.save(request);

        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.username()).isEqualTo("username");
        assertThat(result.name()).isEqualTo("name");
        assertThat(result.code()).isEqualTo("code");

        User savedUser = userRepository.findById(result.id()).orElseThrow();
        assertThat(passwordEncoder.matches("password", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("사용자 정보를 수정할 수 있다")
    void update() {
        // given
        User user = User.builder()
                .username("old_username")
                .password(passwordEncoder.encode("old_password"))
                .name("old_name")
                .code("old_code")
                .build();
        User savedUser = userRepository.save(user);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("new_username")
                .password("new_password")
                .name("new_name")
                .code("new_code")
                .build();

        // when
        UserResponse result = userService.update(savedUser.getId(), request);
        em.flush();
        em.clear();

        // then
        assertThat(result.username()).isEqualTo("new_username");
        assertThat(result.name()).isEqualTo("new_name");
        assertThat(result.code()).isEqualTo("new_code");

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("new_password", updatedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 사용자 수정시 예외가 발생한다")
    void update_notFound() {
        // given
        Long notFoundId = 999L;
        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("new_username")
                .password("new_password")
                .name("new_name")
                .code("new_code")
                .build();

        // when & then
        assertThatThrownBy(() -> userService.update(notFoundId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("사용자를 삭제할 수 있다")
    void delete() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        // when
        userService.delete(savedUser.getId());

        // then
        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제시 예외가 발생한다")
    void delete_notFound() {
        // given
        Long notFoundId = 999L;

        // when & then
        assertThatThrownBy(() -> userService.delete(notFoundId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("사용자에게 역할을 할당할 수 있다")
    void assignRolesToUser() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        UserRoleAssignRequest request = new UserRoleAssignRequest(List.of(savedRole.getId()));

        // when
        UserResponse result = userService.assignRolesToUser(savedUser.getId(), request);

        // then
        assertThat(result.roles()).hasSize(1);
        assertThat(result.roles().get(0).roleName()).isEqualTo("ADMIN");

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getRoles()).hasSize(1);
        assertThat(updatedUser.getRoles().get(0).getRoleName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("사용자의 역할 목록을 조회할 수 있다")
    void getUserRoles() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        savedUser.addRole(savedRole);
        userRepository.saveAndFlush(savedUser);
        em.clear();

        // when
        List<RoleResponse> result = userService.getUserRoles(savedUser.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).roleName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("이미 할당된 역할을 다시 할당하려 할 때 예외가 발생한다")
    void assignRolesToUser_AlreadyExists() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        savedUser.addRole(savedRole);
        em.flush();
        em.clear();

        UserRoleAssignRequest request = new UserRoleAssignRequest(List.of(savedRole.getId()));

        // when & then
        assertThatThrownBy(() -> userService.assignRolesToUser(savedUser.getId(), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Some roles already exist for this user");
    }

    @Test
    @DisplayName("사용자에서 역할을 제거할 수 있다")
    void removeRoleFromUser() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        savedUser.addRole(savedRole);
        em.flush();
        em.clear();

        // when
        userService.removeRoleFromUser(savedUser.getId(), savedRole.getId());

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 역할을 제거하려 할 때 예외가 발생한다")
    void removeRoleFromUser_NotFound() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        // when & then
        assertThatThrownBy(() -> userService.removeRoleFromUser(savedUser.getId(), savedRole.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Role not found for this user");
    }

    @Test
    @DisplayName("사용자명으로 사용자를 조회할 수 있다")
    void findByUsername() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        userRepository.save(user);

        // when
        UserResponse result = userService.findByUsername("username");

        // then
        assertThat(result.username()).isEqualTo("username");
        assertThat(result.name()).isEqualTo("name");
        assertThat(result.code()).isEqualTo("code");
    }

    @Test
    @DisplayName("존재하지 않는 사용자명으로 조회시 예외가 발생한다")
    void findByUsername_NotFound() {
        // given
        String username = "not_exists";

        // when & then
        assertThatThrownBy(() -> userService.findByUsername(username))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with username: " + username);
    }

    @Test
    @DisplayName("일부 필드만 수정할 수 있다")
    void update_PartialUpdate() {
        // given
        User user = User.builder()
                .username("old_username")
                .password(passwordEncoder.encode("old_password"))
                .name("old_name")
                .code("old_code")
                .build();
        User savedUser = userRepository.save(user);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("new_username")
                .password(null)
                .name(null)
                .code(null)
                .build();

        // when
        UserResponse result = userService.update(savedUser.getId(), request);
        em.flush();
        em.clear();

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("new_username");
        assertThat(passwordEncoder.matches("old_password", updatedUser.getPassword())).isTrue();
        assertThat(updatedUser.getName()).isEqualTo("old_name");
        assertThat(updatedUser.getCode()).isEqualTo("old_code");
    }

    @Test
    @DisplayName("빈 문자열은 업데이트하지 않는다")
    void update_IgnoreEmptyStrings() {
        // given
        User user = User.builder()
                .username("old_username")
                .password(passwordEncoder.encode("old_password"))
                .name("old_name")
                .code("old_code")
                .build();
        User savedUser = userRepository.save(user);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .username("")
                .password("")
                .name("")
                .code("")
                .build();

        // when
        UserResponse result = userService.update(savedUser.getId(), request);
        em.flush();
        em.clear();

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("old_username");
        assertThat(passwordEncoder.matches("old_password", updatedUser.getPassword())).isTrue();
        assertThat(updatedUser.getName()).isEqualTo("old_name");
        assertThat(updatedUser.getCode()).isEqualTo("old_code");
    }

    @Test
    @DisplayName("모든 필드가 null이면 아무것도 업데이트하지 않는다")
    void update_IgnoreAllNull() {
        // given
        User user = User.builder()
                .username("old_username")
                .password(passwordEncoder.encode("old_password"))
                .name("old_name")
                .code("old_code")
                .build();
        User savedUser = userRepository.save(user);

        UserUpdateRequest request = UserUpdateRequest.builder()
                .username(null)
                .password(null)
                .name(null)
                .code(null)
                .build();

        // when
        UserResponse result = userService.update(savedUser.getId(), request);
        em.flush();
        em.clear();

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("old_username");
        assertThat(passwordEncoder.matches("old_password", updatedUser.getPassword())).isTrue();
        assertThat(updatedUser.getName()).isEqualTo("old_name");
        assertThat(updatedUser.getCode()).isEqualTo("old_code");
    }

    @Test
    @DisplayName("사용자에게 템플릿을 할당할 수 있다")
    void assignTemplateToUser() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        Template template = Template.builder()
                .name("Test Template")
                .url("http://test.com")
                .build();

        TemplateResponse savedTemplate = templateService.save(new TemplateCreateRequest(template.getName(), template.getUrl()));

        // when
        UserResponse result = userService.assignTemplateToUser(savedUser.getId(), savedTemplate.id());

        // then
        assertThat(result.template()).isNotNull();
        assertThat(result.template().name()).isEqualTo("Test Template");
        assertThat(result.template().url()).isEqualTo("http://test.com");

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getTemplate()).isNotNull();
        assertThat(updatedUser.getTemplate().getName()).isEqualTo("Test Template");
    }

    @Test
    @DisplayName("사용자의 템플릿을 조회할 수 있다")
    void getUserTemplate() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        Template template = Template.builder()
                .name("Test Template")
                .url("http://test.com")
                .build();
        TemplateResponse savedTemplate = templateService.save(new TemplateCreateRequest(template.getName(), template.getUrl()));

        Template templateEntity = templateService.findTemplateById(savedTemplate.id());

        savedUser.changeTemplate(templateEntity);
        em.flush();
        em.clear();

        // when
        TemplateResponse result = userService.getUserTemplate(savedUser.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Test Template");
        assertThat(result.url()).isEqualTo("http://test.com");
    }

    @Test
    @DisplayName("템플릿이 없는 사용자의 템플릿 조회시 예외가 발생한다")
    void getUserTemplate_NotFound() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        // when & then
        assertThatThrownBy(() -> userService.getUserTemplate(savedUser.getId()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Template not found for user");
    }

    @Test
    @DisplayName("사용자의 템플릿을 제거할 수 있다")
    void removeUserTemplate() {
        // given
        User user = User.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .name("name")
                .code("code")
                .build();
        User savedUser = userRepository.save(user);

        Template template = Template.builder()
                .name("Test Template")
                .url("http://test.com")
                .build();

        TemplateResponse response = templateService.save(new TemplateCreateRequest(template.getName(), template.getUrl()));
        Template savedTemplate = templateService.findTemplateById(response.id());

        savedUser.changeTemplate(savedTemplate);
        em.flush();
        em.clear();

        // when
        userService.removeUserTemplate(savedUser.getId());

        // then
        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getTemplate()).isNull();
    }

    @Test
    @DisplayName("사용자 목록 조회시 N+1 문제가 발생하지 않는다")
    void findAll_NoNPlusOne() {
        // given
        // 여러 사용자와 템플릿 생성
        for (int i = 0; i < 5; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .password(passwordEncoder.encode("password" + i))
                    .name("name" + i)
                    .code("code" + i)
                    .build();
            User savedUser = userRepository.save(user);

            Template template = Template.builder()
                    .name("Template " + i)
                    .url("http://test" + i + ".com")
                    .build();
            TemplateResponse savedTemplate = templateService.save(new TemplateCreateRequest(template.getName(), template.getUrl()));
            Template templateEntity = templateService.findTemplateById(savedTemplate.id());
            
            savedUser.changeTemplate(templateEntity);

            // 각 사용자에게 역할 추가
            Role role = Role.builder()
                    .roleName("ROLE_" + i)
                    .build();
            Role savedRole = roleRepository.save(role);

            // 각 역할에 권한 추가
            Permission permission = Permission.builder()
                    .description("PERMISSION_" + i)
                    .build();
            Permission savedPermission = permissionRepository.save(permission);
            savedRole.addPermission(savedPermission);

            savedUser.addRole(savedRole);
        }
        em.flush();
        em.clear();

        // Hibernate Statistics 활성화
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        statistics.setStatisticsEnabled(true);

        // when
        List<UserResponse> users = userService.findAll();

        // then
        assertThat(users).hasSize(5);
        // EntityGraph를 사용하여 단일 쿼리로 조회되어야 함
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
        
        // 모든 사용자의 템플릿, 역할, 권한 정보가 정상적으로 로드되었는지 확인
        assertThat(users).allSatisfy(user -> {
            assertThat(user.template()).isNotNull();
            assertThat(user.template().name()).startsWith("Template ");
            assertThat(user.template().url()).startsWith("http://test");
            assertThat(user.roles()).hasSize(1);
            assertThat(user.roles().get(0).roleName()).startsWith("ROLE_");
            assertThat(user.permissions()).hasSize(1);
            assertThat(user.permissions().get(0).description()).startsWith("PERMISSION_");
        });
    }
}