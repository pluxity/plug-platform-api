package com.pluxity.facility.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.service.FacilityService;
import com.pluxity.file.constant.FileStatus;
import com.pluxity.file.constant.FileType;
import com.pluxity.file.dto.FileResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FacilityController.class)
class FacilityControllerTest {

    private MockMvc mockMvc;
    private FacilityService facilityService;
    private ObjectMapper objectMapper = new ObjectMapper();

    FacilityControllerTest() {
        this.facilityService = mock(FacilityService.class);
        FacilityController controller = new FacilityController(facilityService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("시설 생성 성공 테스트")
    void createFacilitySuccess() throws Exception {
        FacilityCreateRequest request = FacilityCreateRequest.of("테스트 시설", "테스트 시설 설명", 1L, 2L);
        
        FileResponse fileResponse = new FileResponse(
                1L,
                "facilities/1/file",
                "test.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/facilities/1/file",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse = new FileResponse(
                2L,
                "facilities/1/thumbnail",
                "thumbnail.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/facilities/1/thumbnail",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FacilityResponse response = new FacilityResponse(
                1L,
                "테스트 시설",
                "테스트 시설 설명",
                fileResponse,
                thumbnailResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(facilityService.createFacility(any(FacilityCreateRequest.class))).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/facilities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("테스트 시설"))
                .andExpect(jsonPath("$.description").value("테스트 시설 설명"))
                .andExpect(jsonPath("$.file.id").value(1L))
                .andExpect(jsonPath("$.file.fileUrl").value("https://example.com/facilities/1/file"))
                .andExpect(jsonPath("$.thumbnail.id").value(2L))
                .andExpect(jsonPath("$.thumbnail.fileUrl").value("https://example.com/facilities/1/thumbnail"));
        
        verify(facilityService).createFacility(any(FacilityCreateRequest.class));
    }

    @Test
    @DisplayName("시설 조회 성공 테스트")
    void getFacilitySuccess() throws Exception {
        FileResponse fileResponse = new FileResponse(
                1L,
                "facilities/1/file",
                "test.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/facilities/1/file",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse = new FileResponse(
                2L,
                "facilities/1/thumbnail",
                "thumbnail.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/facilities/1/thumbnail",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FacilityResponse response = new FacilityResponse(
                1L,
                "테스트 시설",
                "테스트 시설 설명",
                fileResponse,
                thumbnailResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(facilityService.getFacility(1L)).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/facilities/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("테스트 시설"))
                .andExpect(jsonPath("$.description").value("테스트 시설 설명"))
                .andExpect(jsonPath("$.file.id").value(1L))
                .andExpect(jsonPath("$.file.fileUrl").value("https://example.com/facilities/1/file"))
                .andExpect(jsonPath("$.thumbnail.id").value(2L))
                .andExpect(jsonPath("$.thumbnail.fileUrl").value("https://example.com/facilities/1/thumbnail"));
        
        verify(facilityService).getFacility(1L);
    }

    @Test
    @DisplayName("모든 시설 조회 성공 테스트")
    void getAllFacilitysSuccess() throws Exception {
        FileResponse fileResponse1 = new FileResponse(
                1L,
                "facilities/1/file",
                "test1.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/facilities/1/file",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse1 = new FileResponse(
                2L,
                "facilities/1/thumbnail",
                "thumbnail1.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/facilities/1/thumbnail",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FacilityResponse response1 = new FacilityResponse(
                1L,
                "테스트 시설 1",
                "테스트 시설 설명 1",
                fileResponse1,
                thumbnailResponse1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse fileResponse2 = new FileResponse(
                3L,
                "facilities/2/file",
                "test2.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/facilities/2/file",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse2 = new FileResponse(
                4L,
                "facilities/2/thumbnail",
                "thumbnail2.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/facilities/2/thumbnail",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FacilityResponse response2 = new FacilityResponse(
                2L,
                "테스트 시설 2",
                "테스트 시설 설명 2",
                fileResponse2,
                thumbnailResponse2,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(facilityService.getAllFacilitys()).willReturn(List.of(response1, response2));

        mockMvc.perform(MockMvcRequestBuilders.get("/facilities")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("테스트 시설 1"))
                .andExpect(jsonPath("$[0].file.id").value(1L))
                .andExpect(jsonPath("$[0].thumbnail.id").value(2L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("테스트 시설 2"))
                .andExpect(jsonPath("$[1].file.id").value(3L))
                .andExpect(jsonPath("$[1].thumbnail.id").value(4L));
        
        verify(facilityService).getAllFacilitys();
    }

    @Test
    @DisplayName("시설 업데이트 성공 테스트")
    void updateFacilitySuccess() throws Exception {
        FacilityUpdateRequest request = FacilityUpdateRequest.of("업데이트된 시설", "업데이트된 설명", 3L, 4L);
        
        FileResponse fileResponse = new FileResponse(
                3L,
                "facilities/1/file",
                "updated.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/facilities/1/file/updated",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse = new FileResponse(
                4L,
                "facilities/1/thumbnail",
                "updated_thumbnail.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/facilities/1/thumbnail/updated",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FacilityResponse response = new FacilityResponse(
                1L,
                "업데이트된 시설",
                "업데이트된 설명",
                fileResponse,
                thumbnailResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(facilityService.updateFacility(anyLong(), any(FacilityUpdateRequest.class))).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/facilities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("업데이트된 시설"))
                .andExpect(jsonPath("$.description").value("업데이트된 설명"))
                .andExpect(jsonPath("$.file.id").value(3L))
                .andExpect(jsonPath("$.file.fileUrl").value("https://example.com/facilities/1/file/updated"))
                .andExpect(jsonPath("$.thumbnail.id").value(4L))
                .andExpect(jsonPath("$.thumbnail.fileUrl").value("https://example.com/facilities/1/thumbnail/updated"));
        
        verify(facilityService).updateFacility(anyLong(), any(FacilityUpdateRequest.class));
    }

    @Test
    @DisplayName("시설 삭제 성공 테스트")
    void deleteFacilitySuccess() throws Exception {
        doNothing().when(facilityService).deleteFacility(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/facilities/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(facilityService).deleteFacility(1L);
    }
} 