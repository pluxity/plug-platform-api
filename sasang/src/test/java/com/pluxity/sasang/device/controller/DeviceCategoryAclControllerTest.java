package com.pluxity.sasang.device.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.domains.device_category_acl.device.controller.DeviceCategoryAclController;
import com.pluxity.domains.device_category_acl.device.dto.DeviceCategoryResponseDto;
import com.pluxity.domains.device_category_acl.device.dto.GrantPermissionRequest;
import com.pluxity.domains.device_category_acl.device.dto.RevokePermissionRequest;
import com.pluxity.domains.device_category_acl.device.service.DeviceCategoryAclService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = DeviceCategoryAclController.class,
    excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class}
)
@ContextConfiguration(classes = {
    DeviceCategoryAclController.class,
    com.pluxity.global.exception.CustomExceptionHandler.class
})
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true",
})
class DeviceCategoryAclControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeviceCategoryAclService deviceCategoryAclService;

    // =========== 권한 부여 테스트 ===========

    @Test
    @DisplayName("디바이스 카테고리에 대한 권한 부여 요청 시 성공적으로 처리된다")
    void grantPermission_ValidRequest_Success() throws Exception {
        // given
        GrantPermissionRequest request = createGrantPermissionRequest();
        
        doNothing().when(deviceCategoryAclService).grantPermission(any(GrantPermissionRequest.class));

        // when
        ResultActions result = mockMvc.perform(post("/acl/device-categories/grant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());
        
        verify(deviceCategoryAclService).grantPermission(any(GrantPermissionRequest.class));
    }
    
    @Test
    @DisplayName("디바이스 카테고리에 대한 권한 부여 요청 시 권한이 없으면 예외가 발생한다")
    void grantPermission_NoPermission_ThrowsException() throws Exception {
        // given
        GrantPermissionRequest request = createGrantPermissionRequest();
        
        doThrow(new AccessDeniedException("Access denied")).when(deviceCategoryAclService)
            .grantPermission(any(GrantPermissionRequest.class));

        // when
        ResultActions result = mockMvc.perform(post("/acl/device-categories/grant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isForbidden());
        
        verify(deviceCategoryAclService).grantPermission(any(GrantPermissionRequest.class));
    }
    
    @Test
    @DisplayName("디바이스 카테고리에 대한 권한 부여 요청 시 유효하지 않은 요청은 400 응답을 반환한다")
    void grantPermission_InvalidRequest_ReturnsBadRequest() throws Exception {
        // given
        GrantPermissionRequest request = new GrantPermissionRequest(null, null, null, false, null);
        
        // when
        ResultActions result = mockMvc.perform(post("/acl/device-categories/grant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
        
        verify(deviceCategoryAclService, never()).grantPermission(any(GrantPermissionRequest.class));
    }

    // =========== 권한 회수 테스트 ===========

    @Test
    @DisplayName("디바이스 카테고리에 대한 권한 회수 요청 시 성공적으로 처리된다")
    void revokePermission_ValidRequest_Success() throws Exception {
        // given
        RevokePermissionRequest request = createRevokePermissionRequest();
        
        doNothing().when(deviceCategoryAclService).revokePermission(any(RevokePermissionRequest.class));

        // when
        ResultActions result = mockMvc.perform(post("/acl/device-categories/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk());
        
        verify(deviceCategoryAclService).revokePermission(any(RevokePermissionRequest.class));
    }
    
    @Test
    @DisplayName("디바이스 카테고리에 대한 권한 회수 요청 시 권한이 없으면 예외가 발생한다")
    void revokePermission_NoPermission_ThrowsException() throws Exception {
        // given
        RevokePermissionRequest request = createRevokePermissionRequest();
        
        doThrow(new AccessDeniedException("Access denied")).when(deviceCategoryAclService)
            .revokePermission(any(RevokePermissionRequest.class));

        // when
        ResultActions result = mockMvc.perform(post("/acl/device-categories/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isForbidden());
        
        verify(deviceCategoryAclService).revokePermission(any(RevokePermissionRequest.class));
    }
    
    @Test
    @DisplayName("디바이스 카테고리에 대한 권한 회수 요청 시 유효하지 않은 요청은 400 응답을 반환한다")
    void revokePermission_InvalidRequest_ReturnsBadRequest() throws Exception {
        // given
        RevokePermissionRequest request = new RevokePermissionRequest(null, null, null, false, null, false);
        
        // when
        ResultActions result = mockMvc.perform(post("/acl/device-categories/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
        
        verify(deviceCategoryAclService, never()).revokePermission(any(RevokePermissionRequest.class));
    }

    // =========== 읽기 권한 확인 테스트 ===========

    @Test
    @DisplayName("디바이스 카테고리에 대한 읽기 권한 확인 시 성공적으로 반환한다")
    void checkReadPermission_ReturnsPermissionStatus() throws Exception {
        // given
        Long categoryId = 1L;
        boolean hasPermission = true;
        
        given(deviceCategoryAclService.hasReadPermission(categoryId)).willReturn(hasPermission);

        // when
        ResultActions result = mockMvc.perform(get("/acl/device-categories/{deviceCategoryId}/check-read", categoryId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(true)));
        
        verify(deviceCategoryAclService).hasReadPermission(categoryId);
    }
    
    @Test
    @DisplayName("디바이스 카테고리에 대한 읽기 권한이 없을 때 확인 시 false를 반환한다")
    void checkReadPermission_NoPermission_ReturnsFalse() throws Exception {
        // given
        Long categoryId = 1L;
        boolean hasPermission = false;
        
        given(deviceCategoryAclService.hasReadPermission(categoryId)).willReturn(hasPermission);

        // when
        ResultActions result = mockMvc.perform(get("/acl/device-categories/{deviceCategoryId}/check-read", categoryId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data", is(false)));
        
        verify(deviceCategoryAclService).hasReadPermission(categoryId);
    }
    
    @Test
    @DisplayName("존재하지 않는 디바이스 카테고리의 읽기 권한 확인 시 예외가 발생한다")
    void checkReadPermission_NonExistingCategory_ThrowsException() throws Exception {
        // given
        Long categoryId = 999L;
        
        given(deviceCategoryAclService.hasReadPermission(categoryId))
            .willThrow(new IllegalArgumentException("Category not found"));

        // when
        ResultActions result = mockMvc.perform(get("/acl/device-categories/{deviceCategoryId}/check-read", categoryId)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isNotFound());
        
        verify(deviceCategoryAclService).hasReadPermission(categoryId);
    }

    // =========== 카테고리 목록 조회 테스트 ===========

    @Test
    @DisplayName("현재 사용자가 접근 가능한 디바이스 카테고리 목록 조회 시 성공적으로 반환한다")
    void getMyDeviceCategories_ReturnsCategories() throws Exception {
        // given
        List<DeviceCategoryResponseDto> categories = Arrays.asList(
            new DeviceCategoryResponseDto(1L, "카테고리1"),
            new DeviceCategoryResponseDto(2L, "카테고리2")
        );
        
        given(deviceCategoryAclService.findAllAllowedForCurrentUser()).willReturn(categories);

        // when
        ResultActions result = mockMvc.perform(get("/acl/device-categories/mine")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].name", is("카테고리1")))
                .andExpect(jsonPath("$.data[1].id", is(2)))
                .andExpect(jsonPath("$.data[1].name", is("카테고리2")));
        
        verify(deviceCategoryAclService).findAllAllowedForCurrentUser();
    }
    
    @Test
    @DisplayName("접근 가능한 디바이스 카테고리가 없을 때 빈 목록을 반환한다")
    void getMyDeviceCategories_NoAccessibleCategories_ReturnsEmptyList() throws Exception {
        // given
        List<DeviceCategoryResponseDto> categories = Collections.emptyList();
        
        given(deviceCategoryAclService.findAllAllowedForCurrentUser()).willReturn(categories);

        // when
        ResultActions result = mockMvc.perform(get("/acl/device-categories/mine")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)));
        
        verify(deviceCategoryAclService).findAllAllowedForCurrentUser();
    }
    
    @Test
    @DisplayName("미인증 사용자의 디바이스 카테고리 목록 조회 시 예외가 발생한다")
    void getMyDeviceCategories_Unauthenticated_ThrowsException() throws Exception {
        // given
        given(deviceCategoryAclService.findAllAllowedForCurrentUser())
            .willThrow(new AccessDeniedException("Not authenticated"));

        // when
        ResultActions result = mockMvc.perform(get("/acl/device-categories/mine")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isForbidden());
        
        verify(deviceCategoryAclService).findAllAllowedForCurrentUser();
    }
    
    // =========== 헬퍼 메소드 ===========
    
    private GrantPermissionRequest createGrantPermissionRequest() {
        return new GrantPermissionRequest(
            "DeviceCategory",
            1L,
            "user1",
            false,
            List.of("READ", "WRITE")
        );
    }
    
    private RevokePermissionRequest createRevokePermissionRequest() {
        return new RevokePermissionRequest(
            "DeviceCategory",
            1L,
            "user1",
            false,
            List.of("READ", "WRITE"),
            false
        );
    }
}