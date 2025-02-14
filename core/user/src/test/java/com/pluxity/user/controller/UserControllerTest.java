package com.pluxity.user.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.TestApplication;
import com.pluxity.global.config.GlobalBeanConfig;
import com.pluxity.user.dto.TemplateCreateRequest;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.Template;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.RoleRepository;
import com.pluxity.user.repository.TemplateRepository;
import com.pluxity.user.repository.UserRepository;
import com.pluxity.user.service.TemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {TestApplication.class})
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@Import(GlobalBeanConfig.class)
@AutoConfigureRestDocs
@Transactional
@WithMockUser(username = "test", roles = "ADMIN")
class UserControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private TemplateRepository templateRepository;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    @DisplayName("모든 사용자 조회")
    void getAllUsers() throws Exception {
        // given
        User user1 = createUser("user1", "password1", "User One", "CODE1");
        User user2 = createUser("user2", "password2", "User Two", "CODE2");
        userRepository.saveAll(List.of(user1, user2));

        // when & then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andDo(document("user-list",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .summary("사용자 목록 조회 API")
                                .responseFields(
                                        fieldWithPath("[].id").type(NUMBER).description("사용자 ID"),
                                        fieldWithPath("[].username").type(STRING).description("사용자명"),
                                        fieldWithPath("[].name").type(STRING).description("이름"),
                                        fieldWithPath("[].code").type(STRING).description("코드"),
                                        fieldWithPath("[].roles").type(ARRAY).description("사용자 역할 목록"),
                                        fieldWithPath("[].template").type(OBJECT).optional().description("사용자 템플릿 (없을 수 있음)"),
                                        fieldWithPath("[].permissions").type(ARRAY).optional().description("사용자 권한 (없을 수 있음)")
                                )
                                .build())));
    }

    @Test
    @DisplayName("ID로 사용자 조회")
    void getUserById() throws Exception {
        // given
        User user = createUser("testuser", "password", "Test User", "TEST123");
        User savedUser = userRepository.save(user);

        // when & then
        mockMvc.perform(get("/admin/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andDo(document("user-get",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(parameterWithName("id").description("사용자 ID"))
                                .responseFields(
                                        fieldWithPath("id").type(NUMBER).description("사용자 ID"),
                                        fieldWithPath("username").type(STRING).description("사용자명"),
                                        fieldWithPath("name").type(STRING).description("이름"),
                                        fieldWithPath("code").type(STRING).description("코드"),
                                        fieldWithPath("roles").type(ARRAY).description("사용자 역할 목록"),
                                        fieldWithPath("template").type(OBJECT).optional().description("사용자 템플릿 (없을 수 있음)"),
                                        fieldWithPath("permissions").type(ARRAY).optional().description("사용자 권한 (없을 수 있음)")
                                )
                                .build())));
    }

    @Test
    @DisplayName("새 사용자 생성")
    void createUser() throws Exception {
        // given
        Map<String, Object> request = Map.of(
                "username", "newuser",
                "password", "newpass",
                "name", "New User",
                "code", "NEW123"
        );

        // when & then
        mockMvc.perform(post("/admin/users")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("user-create",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .requestFields(
                                        fieldWithPath("username").type(STRING).description("사용자명"),
                                        fieldWithPath("password").type(STRING).description("비밀번호"),
                                        fieldWithPath("name").type(STRING).description("이름"),
                                        fieldWithPath("code").type(STRING).description("코드")
                                )
                                .build())));
    }

    @Test
    @DisplayName("사용자 정보 수정")
    void updateUser() throws Exception {
        // given
        User user = createUser("olduser", "oldpass", "Old User", "OLD123");
        User savedUser = userRepository.save(user);

        Map<String, Object> updateRequest = Map.of(
                "username", "updateduser",
                "name", "Updated User",
                "code", "UPD123"
        );

        // when & then
        mockMvc.perform(put("/admin/users/{id}", savedUser.getId())
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andDo(document("user-update",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(parameterWithName("id").description("사용자 ID"))
                                .requestFields(
                                        fieldWithPath("username").type(STRING).description("사용자명"),
                                        fieldWithPath("name").type(STRING).description("이름"),
                                        fieldWithPath("code").type(STRING).description("코드")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(NUMBER).description("사용자 ID"),
                                        fieldWithPath("username").type(STRING).description("사용자명"),
                                        fieldWithPath("name").type(STRING).description("이름"),
                                        fieldWithPath("code").type(STRING).description("코드"),
                                        fieldWithPath("roles").type(ARRAY).description("사용자 역할 목록"),
                                        fieldWithPath("template").type(OBJECT).optional().description("사용자 템플릿 (없을 수 있음)"),
                                        fieldWithPath("permissions").type(ARRAY).optional().description("사용자 권한 (없을 수 있음)")
                                )
                                .build())));
    }

    @Test
    @DisplayName("사용자 비밀번호 수정")
    void updateUserPassword() throws Exception {
        // given
        User user = createUser("user", "oldpass", "User", "USER123");
        User savedUser = userRepository.save(user);

        Map<String, String> updateRequest = Map.of("password", "newpass");

        // when & then
        mockMvc.perform(put("/admin/users/{id}/password", savedUser.getId())
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andDo(document("user-update-password",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(parameterWithName("id").description("사용자 ID"))
                                .requestFields(
                                        fieldWithPath("password").type(STRING).description("새 비밀번호")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(NUMBER).description("사용자 ID"),
                                        fieldWithPath("username").type(STRING).description("사용자명"),
                                        fieldWithPath("name").type(STRING).description("이름"),
                                        fieldWithPath("code").type(STRING).description("코드"),
                                        fieldWithPath("roles").type(ARRAY).description("사용자 역할 목록"),
                                        fieldWithPath("template").type(OBJECT).optional().description("사용자 템플릿 (없을 수 있음)"),
                                        fieldWithPath("permissions").type(ARRAY).optional().description("사용자 권한 (없을 수 있음)")
                                )
                                .build())));
    }

    @Test
    @DisplayName("사용자 역할 업데이트")
    void updateUserRoles() throws Exception {
        // given
        User user = createUser("roleuser", "rolepass", "Role User", "ROLE123");
        User savedUser = userRepository.save(user);
        Role role1 = roleRepository.save(Role.builder().roleName("ADMIN").build());
        Role role2 = roleRepository.save(Role.builder().roleName("USER").build());

        Map<String, Object> request = Map.of("roleIds", List.of(role1.getId(), role2.getId()));

        // when & then
        mockMvc.perform(put("/admin/users/{userId}/roles", savedUser.getId())
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document("user-update-roles",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(parameterWithName("userId").description("사용자 ID"))
                                .requestFields(
                                        fieldWithPath("roleIds").type(ARRAY).description("역할 ID 목록")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(NUMBER).description("사용자 ID"),
                                        fieldWithPath("username").type(STRING).description("사용자명"),
                                        fieldWithPath("name").type(STRING).description("이름"),
                                        fieldWithPath("code").type(STRING).description("코드"),
                                        fieldWithPath("roles").type(ARRAY).description("사용자 역할 목록"),
                                        fieldWithPath("roles[].id").type(NUMBER).description("역할 ID"),
                                        fieldWithPath("roles[].roleName").type(STRING).description("역할 이름"),
                                        fieldWithPath("roles[].permissions").type(ARRAY).description("역할에 할당된 권한 목록"),
                                        fieldWithPath("template").type(OBJECT).optional().description("사용자 템플릿 (없을 수 있음)"),
                                        fieldWithPath("permissions").type(ARRAY).optional().description("사용자 권한 (없을 수 있음)")
                                )
                                .build())));
    }

    @Test
    @DisplayName("사용자 삭제")
    void deleteUser() throws Exception {
        // given
        User user = createUser("deleteuser", "delpass", "test", "DEL123");
        User savedUser = userRepository.save(user);

        // when & then
        mockMvc.perform(delete("/admin/users/{id}", savedUser.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(document("user-delete",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(parameterWithName("id").description("사용자 ID"))
                                .build())));
    }

    @Test
    @DisplayName("사용자 역할 할당")
    void assignRolesToUser() throws Exception {
        // given
        User user = createUser("roleuser", "rolepass", "Role User", "ROLE123");
        User savedUser = userRepository.save(user);
        Role role = roleRepository.save(Role.builder().roleName("ADMIN").build());

        Map<String, Object> request = Map.of("roleIds", List.of(role.getId()));

        // when & then
        mockMvc.perform(post("/admin/users/{userId}/roles", savedUser.getId())
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andDo(document("user-assign-roles",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(parameterWithName("userId").description("사용자 ID"))
                                .requestFields(
                                        fieldWithPath("roleIds").type(ARRAY).description("역할 ID 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("사용자 역할 제거")
    void removeRoleFromUser() throws Exception {
        // given
        User user = createUser("roleuser", "rolepass", "Role User", "ROLE123");
        User savedUser = userRepository.save(user);
        Role role = roleRepository.save(Role.builder().roleName("ADMIN").build());
        user.addRole(role);

        // when & then
        mockMvc.perform(delete("/admin/users/{userId}/roles/{roleId}", savedUser.getId(), role.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(document("user-remove-role",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(
                                        parameterWithName("userId").description("사용자 ID"),
                                        parameterWithName("roleId").description("역할 ID")
                                )
                                .build())));
    }

    @Test
    @DisplayName("사용자에게 템플릿 할당")
    void assignTemplateToUser() throws Exception {
        // given
        User user = createUser("templateuser", "templatepass", "template", "TEMP123");
        User savedUser = userRepository.save(user);
        Long templateId = templateService.save(new TemplateCreateRequest("Test Template", "http://test.com")).id();

        // when & then
        mockMvc.perform(post("/admin/users/{userId}/template/{templateId}", savedUser.getId(), templateId)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andDo(document("user-assign-template",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(
                                        parameterWithName("userId").description("사용자 ID"),
                                        parameterWithName("templateId").description("템플릿 ID")
                                )
                                .build())));
    }

    @Test
    @DisplayName("사용자의 템플릿 조회")
    void getUserTemplate() throws Exception {
        // given
        User user = createUser("templateuser", "templatepass", "template", "TEMP123");
        User savedUser = userRepository.save(user);
        Long templateId = templateService.save(new TemplateCreateRequest("Test Template", "http://test.com")).id();
        Template template = templateRepository.findById(templateId).orElseThrow();
        savedUser.changeTemplate(template);
        userRepository.save(savedUser);

        // when & then
        mockMvc.perform(get("/admin/users/{userId}/template", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Template"))
                .andExpect(jsonPath("$.thumbnail").value("http://test.com"))
                .andDo(document("user-get-template",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(
                                        parameterWithName("userId").description("사용자 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(NUMBER).description("템플릿 ID"),
                                        fieldWithPath("name").type(STRING).description("템플릿 이름"),
                                        fieldWithPath("thumbnail").type(STRING).description("템플릿 URL")
                                )
                                .build())));
    }

    @Test
    @DisplayName("사용자의 템플릿 제거")
    void removeUserTemplate() throws Exception {
        // given
        User user = createUser("templateuser", "templatepass", "template", "TEMP123");
        User savedUser = userRepository.save(user);
        Long templateId = templateService.save(new TemplateCreateRequest("Test Template", "http://test.com")).id();
        Template template = templateRepository.findById(templateId).orElseThrow();
        savedUser.changeTemplate(template);
        userRepository.save(savedUser);

        // when & then
        mockMvc.perform(delete("/admin/users/{userId}/template", savedUser.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andDo(document("user-remove-template",
                        resource(ResourceSnippetParameters.builder()
                                .tag("user")
                                .pathParameters(
                                        parameterWithName("userId").description("사용자 ID")
                                )
                                .build())));
    }

    private User createUser(String username, String password, String name, String code) {
        return User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name(name)
                .code(code)
                .build();
    }
}