package com.pluxity.user.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.user.dto.RequestRole;
import com.pluxity.user.dto.RequestRolePermissions;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.entity.Role;
import com.pluxity.user.repository.PermissionRepository;
import com.pluxity.user.repository.RoleRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@AutoConfigureRestDocs
@Transactional
@WithMockUser(username = "test", roles = "ADMIN")
class RoleControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private EntityManager em;

    private MockMvc mockMvc;

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
    @DisplayName("역할 목록을 조회할 수 있다")
    void getAllRoles() throws Exception {
        // given
        Role role1 = Role.builder().roleName("ADMIN").build();
        Role role2 = Role.builder().roleName("USER").build();
        roleRepository.saveAll(List.of(role1, role2));

        // when & then
        mockMvc.perform(get("/roles"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].roleName", containsInAnyOrder("ADMIN", "USER")))
                .andDo(MockMvcRestDocumentationWrapper.document("role-list",
                        resource(ResourceSnippetParameters.builder()
                                .tag("role")
                                .summary("역할 목록 조회 API")
                                .description("시스템에 등록된 모든 역할 목록을 조회합니다.")
                                .responseFields(
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("역할 ID"),
                                        fieldWithPath("[].roleName").type(JsonFieldType.STRING).description("역할 이름"),
                                        fieldWithPath("[].permissions").type(JsonFieldType.ARRAY).description("역할에 할당된 권한 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("ID로 역할을 조회할 수 있다")
    void getRole() throws Exception {
        // given
        Role role = Role.builder()
                .roleName("ADMIN")
                .build();
        Role savedRole = roleRepository.save(role);

        // when & then
        mockMvc.perform(get("/roles/{id}", savedRole.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedRole.getId()))
                .andExpect(jsonPath("$.roleName").value("ADMIN"))
                .andDo(MockMvcRestDocumentationWrapper.document("role-get",
                        resource(ResourceSnippetParameters.builder()
                                .tag("role")
                                .summary("역할 상세 조회 API")
                                .description("특정 역할의 상세 정보를 조회합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("역할 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("역할 ID"),
                                        fieldWithPath("roleName").type(JsonFieldType.STRING).description("역할 이름"),
                                        fieldWithPath("permissions").type(JsonFieldType.ARRAY).description("역할에 할당된 권한 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("새로운 역할을 생성할 수 있다")
    void createRole() throws Exception {
        // given
        RequestRole request = new RequestRole("ADMIN");

        // when & then
        mockMvc.perform(post("/roles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.roleName").value("ADMIN"))
                .andDo(MockMvcRestDocumentationWrapper.document("role-create",
                        resource(ResourceSnippetParameters.builder()
                                .tag("role")
                                .summary("역할 생성 API")
                                .description("새로운 역할을 생성합니다.")
                                .requestFields(
                                        fieldWithPath("roleName").type(JsonFieldType.STRING).description("역할 이름")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("생성된 역할 ID"),
                                        fieldWithPath("roleName").type(JsonFieldType.STRING).description("역할 이름"),
                                        fieldWithPath("permissions").type(JsonFieldType.ARRAY).description("역할에 할당된 권한 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("역할 정보를 수정할 수 있다")
    void updateRole() throws Exception {
        // given
        Role role = Role.builder()
                .roleName("OLD_ROLE")
                .build();
        Role savedRole = roleRepository.save(role);
        RequestRole request = new RequestRole("UPDATED_ROLE");

        // when & then
        mockMvc.perform(put("/roles/{id}", savedRole.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("UPDATED_ROLE"))
                .andDo(MockMvcRestDocumentationWrapper.document("role-update",
                        resource(ResourceSnippetParameters.builder()
                                .tag("role")
                                .summary("역할 수정 API")
                                .description("기존 역할의 정보를 수정합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("수정할 역할 ID")
                                )
                                .requestFields(
                                        fieldWithPath("roleName").type(JsonFieldType.STRING).description("수정할 역할 이름")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("역할 ID"),
                                        fieldWithPath("roleName").type(JsonFieldType.STRING).description("수정된 역할 이름"),
                                        fieldWithPath("permissions").type(JsonFieldType.ARRAY).description("역할에 할당된 권한 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("역할을 삭제할 수 있다")
    void deleteRole() throws Exception {
        // given
        Role role = Role.builder()
                .roleName("TO_BE_DELETED")
                .build();
        Role savedRole = roleRepository.save(role);

        // when & then
        mockMvc.perform(delete("/roles/{id}", savedRole.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("role-delete",
                        resource(ResourceSnippetParameters.builder()
                                .tag("role")
                                .summary("역할 삭제 API")
                                .description("기존 역할을 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("삭제할 역할 ID")
                                )
                                .build())));
    }

    @Test
    @DisplayName("역할에 권한을 할당할 수 있다")
    void assignPermissionsToRole() throws Exception {
        // given
        Role role = Role.builder().roleName("ADMIN").build();
        Role savedRole = roleRepository.save(role);

        Permission permission1 = Permission.builder().description("READ_USER").build();
        Permission permission2 = Permission.builder().description("WRITE_USER").build();
        permissionRepository.saveAll(List.of(permission1, permission2));

        RequestRolePermissions request = new RequestRolePermissions(
                List.of(permission1.getId(), permission2.getId())
        );

        // when & then
        mockMvc.perform(post("/roles/{roleId}/permissions", savedRole.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions", hasSize(2)))
                .andDo(MockMvcRestDocumentationWrapper.document("role-assign-permissions",
                        resource(ResourceSnippetParameters.builder()
                                .tag("role")
                                .summary("역할에 권한 할당 API")
                                .description("특정 역할에 여러 권한을 할당합니다.")
                                .pathParameters(
                                        parameterWithName("roleId").description("권한을 할당할 역할 ID")
                                )
                                .requestFields(
                                        fieldWithPath("permissionIds").type(JsonFieldType.ARRAY).description("할당할 권한 ID 목록")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("역할 ID"),
                                        fieldWithPath("roleName").type(JsonFieldType.STRING).description("역할 이름"),
                                        fieldWithPath("permissions").type(JsonFieldType.ARRAY).description("역할에 할당된 권한 목록"),
                                        fieldWithPath("permissions[].id").type(JsonFieldType.NUMBER).description("권한 ID"),
                                        fieldWithPath("permissions[].description").type(JsonFieldType.STRING).description("권한 설명")
                                )
                                .build())));
    }

    @Test
    @DisplayName("역할의 권한 목록을 조회할 수 있다")
    void getRolePermissions() throws Exception {
        // given
        Role role = Role.builder().roleName("ADMIN").build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder().description("READ_USER").build();
        permissionRepository.save(permission);
        role.addPermission(permission);

        // when & then
        mockMvc.perform(get("/roles/{roleId}/permissions", savedRole.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("READ_USER"))
                .andDo(MockMvcRestDocumentationWrapper.document("role-get-permissions",
                        resource(ResourceSnippetParameters.builder()
                                .tag("role")
                                .summary("역할의 권한 목록 조회 API")
                                .description("특정 역할에 할당된 모든 권한 목록을 조회합니다.")
                                .pathParameters(
                                        parameterWithName("roleId").description("조회할 역할 ID")
                                )
                                .responseFields(
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("권한 ID"),
                                        fieldWithPath("[].description").type(JsonFieldType.STRING).description("권한 설명"),
                                        fieldWithPath("[].roles").type(JsonFieldType.ARRAY).description("권한에 할당된 역할 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("역할에서 권한을 제거할 수 있다")
    void removePermissionFromRole() throws Exception {
        // given
        Role role = Role.builder().roleName("ADMIN").build();
        Role savedRole = roleRepository.save(role);

        Permission permission = Permission.builder().description("READ_USER").build();
        Permission savedPermission = permissionRepository.save(permission);
        role.addPermission(permission);

        // when & then
        mockMvc.perform(delete("/roles/{roleId}/permissions/{permissionId}",
                        savedRole.getId(), savedPermission.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("role-remove-permission",
                        resource(ResourceSnippetParameters.builder()
                                .tag("role")
                                .summary("역할에서 권한 제거 API")
                                .description("특정 역할에서 특정 권한을 제거합니다.")
                                .pathParameters(
                                        parameterWithName("roleId").description("권한을 제거할 역할 ID"),
                                        parameterWithName("permissionId").description("제거할 권한 ID")
                                )
                                .build())));
    }
}