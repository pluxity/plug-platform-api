package com.pluxity.user.controller;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.user.dto.PermissionCreateRequest;
import com.pluxity.user.entity.Permission;
import com.pluxity.user.repository.PermissionRepository;
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
class PermissionControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("권한 목록을 조회할 수 있다")
    void getAllPermissions() throws Exception {
        // given
        Permission permission1 = Permission.builder().description("READ_USER").build();
        Permission permission2 = Permission.builder().description("WRITE_USER").build();
        permissionRepository.save(permission1);
        permissionRepository.save(permission2);

        // when & then
        mockMvc.perform(get("/permissions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder("READ_USER", "WRITE_USER")))
                .andDo(MockMvcRestDocumentationWrapper.document("permission-list",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("권한 목록 조회 API")
                                .description("시스템에 등록된 모든 권한 목록을 조회합니다.")
                                .responseFields(
                                        fieldWithPath("[].id").type(JsonFieldType.NUMBER).description("권한 ID"),
                                        fieldWithPath("[].description").type(JsonFieldType.STRING).description("권한 설명"),
                                        fieldWithPath("[].roles").type(JsonFieldType.ARRAY).description("권한에 할당된 역할 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("ID로 권한을 조회할 수 있다")
    void getPermission() throws Exception {
        // given
        Permission permission = Permission.builder()
                .description("READ_USER")
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        // when & then
        mockMvc.perform(get("/permissions/{id}", savedPermission.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPermission.getId()))
                .andExpect(jsonPath("$.description").value("READ_USER"))
                .andDo(MockMvcRestDocumentationWrapper.document("permission-get",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("권한 상세 조회 API")
                                .description("특정 권한의 상세 정보를 조회합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("권한 ID")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("권한 ID"),
                                        fieldWithPath("description").type(JsonFieldType.STRING).description("권한 설명"),
                                        fieldWithPath("roles").type(JsonFieldType.ARRAY).description("권한에 할당된 역할 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("존재하지 않는 권한 조회시 404 응답을 받는다")
    void getPermission_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/permissions/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(MockMvcRestDocumentationWrapper.document("permission-get-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("존재하지 않는 권한 조회 API")
                                .description("존재하지 않는 권한을 조회할 경우 404 응답을 반환합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("존재하지 않는 권한 ID")
                                )
                                .build())));
    }

    @Test
    @DisplayName("새로운 권한을 생성할 수 있다")
    void createPermission() throws Exception {
        // given
        PermissionCreateRequest request = new PermissionCreateRequest("READ_USER");

        // when & then
        mockMvc.perform(post("/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andDo(MockMvcRestDocumentationWrapper.document("permission-create",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("권한 생성 API")
                                .description("새로운 권한을 생성합니다.")
                                .requestFields(
                                        fieldWithPath("description").type(JsonFieldType.STRING).description("권한 설명")
                                )
                                .build())));
    }

    @Test
    @DisplayName("잘못된 요청으로 권한 생성시 400 응답을 받는다")
    void createPermission_BadRequest() throws Exception {
        // given
        PermissionCreateRequest request = new PermissionCreateRequest("");  // 빈 문자열

        // when & then
        mockMvc.perform(post("/permissions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(MockMvcRestDocumentationWrapper.document("permission-create-bad-request",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("잘못된 권한 생성 요청 API")
                                .description("잘못된 형식의 권한 생성 요청시 400 응답을 반환합니다.")
                                .requestFields(
                                        fieldWithPath("description").type(JsonFieldType.STRING).description("권한 설명 (빈 문자열)")
                                )
                                .build())));
    }

    @Test
    @DisplayName("권한 정보를 수정할 수 있다")
    void updatePermission() throws Exception {
        // given
        Permission permission = Permission.builder()
                .description("OLD_PERMISSION")
                .build();
        Permission savedPermission = permissionRepository.save(permission);
        PermissionCreateRequest request = new PermissionCreateRequest("UPDATED_PERMISSION");

        // when & then
        mockMvc.perform(put("/permissions/{id}", savedPermission.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(MockMvcRestDocumentationWrapper.document("permission-update",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("권한 수정 API")
                                .description("기존 권한의 정보를 수정합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("수정할 권한 ID")
                                )
                                .requestFields(
                                        fieldWithPath("description").type(JsonFieldType.STRING).description("수정할 권한 설명")
                                )
                                .responseFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("권한 ID"),
                                        fieldWithPath("description").type(JsonFieldType.STRING).description("권한 설명"),
                                        fieldWithPath("roles").type(JsonFieldType.ARRAY).description("권한에 할당된 역할 목록")
                                )
                                .build())));
    }

    @Test
    @DisplayName("존재하지 않는 권한 수정시 404 응답을 받는다")
    void updatePermission_NotFound() throws Exception {
        // given
        PermissionCreateRequest request = new PermissionCreateRequest("UPDATED_PERMISSION");

        // when & then
        mockMvc.perform(put("/permissions/{id}", 999L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(MockMvcRestDocumentationWrapper.document("permission-update-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("존재하지 않는 권한 수정 API")
                                .description("존재하지 않는 권한 수정 요청시 404 응답을 반환합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("존재하지 않는 권한 ID")
                                )
                                .requestFields(
                                        fieldWithPath("description").type(JsonFieldType.STRING).description("수정할 권한 설명")
                                )
                                .build())));
    }

    @Test
    @DisplayName("권한을 삭제할 수 있다")
    void deletePermission() throws Exception {
        // given
        Permission permission = Permission.builder()
                .description("TO_BE_DELETED")
                .build();
        Permission savedPermission = permissionRepository.save(permission);

        // when & then
        mockMvc.perform(delete("/permissions/{id}", savedPermission.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andDo(MockMvcRestDocumentationWrapper.document("permission-delete",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("권한 삭제 API")
                                .description("기존 권한을 삭제합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("삭제할 권한 ID")
                                )
                                .build())));
    }

    @Test
    @DisplayName("존재하지 않는 권한 삭제시 404 응답을 받는다")
    void deletePermission_NotFound() throws Exception {
        // when & then
        mockMvc.perform(delete("/permissions/{id}", 999L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(MockMvcRestDocumentationWrapper.document("permission-delete-not-found",
                        resource(ResourceSnippetParameters.builder()
                                .tag("permission")
                                .summary("존재하지 않는 권한 삭제 API")
                                .description("존재하지 않는 권한 삭제 요청시 404 응답을 반환합니다.")
                                .pathParameters(
                                        parameterWithName("id").description("존재하지 않는 권한 ID")
                                )
                                .build())));
    }
}