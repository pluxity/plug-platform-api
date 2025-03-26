package com.pluxity.building.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingResponse;
import com.pluxity.building.dto.BuildingUpdateRequest;
import com.pluxity.building.service.BuildingService;
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

@WebMvcTest(controllers = BuildingController.class)
class BuildingControllerTest {

    private MockMvc mockMvc;
    private BuildingService buildingService;
    private ObjectMapper objectMapper = new ObjectMapper();

    BuildingControllerTest() {
        this.buildingService = mock(BuildingService.class);
        BuildingController controller = new BuildingController(buildingService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("빌딩 생성 성공 테스트")
    void createBuildingSuccess() throws Exception {
        BuildingCreateRequest request = BuildingCreateRequest.of("테스트 빌딩", "테스트 빌딩 설명", 1L, 2L);
        
        FileResponse fileResponse = new FileResponse(
                1L,
                "buildings/1/file",
                "test.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/buildings/1/file",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse = new FileResponse(
                2L,
                "buildings/1/thumbnail",
                "thumbnail.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/buildings/1/thumbnail",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        BuildingResponse response = new BuildingResponse(
                1L,
                "테스트 빌딩",
                "테스트 빌딩 설명",
                fileResponse,
                thumbnailResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(buildingService.createBuilding(any(BuildingCreateRequest.class))).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/buildings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("테스트 빌딩"))
                .andExpect(jsonPath("$.description").value("테스트 빌딩 설명"))
                .andExpect(jsonPath("$.file.id").value(1L))
                .andExpect(jsonPath("$.file.fileUrl").value("https://example.com/buildings/1/file"))
                .andExpect(jsonPath("$.thumbnail.id").value(2L))
                .andExpect(jsonPath("$.thumbnail.fileUrl").value("https://example.com/buildings/1/thumbnail"));
        
        verify(buildingService).createBuilding(any(BuildingCreateRequest.class));
    }

    @Test
    @DisplayName("빌딩 조회 성공 테스트")
    void getBuildingSuccess() throws Exception {
        FileResponse fileResponse = new FileResponse(
                1L,
                "buildings/1/file",
                "test.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/buildings/1/file",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse = new FileResponse(
                2L,
                "buildings/1/thumbnail",
                "thumbnail.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/buildings/1/thumbnail",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        BuildingResponse response = new BuildingResponse(
                1L,
                "테스트 빌딩",
                "테스트 빌딩 설명",
                fileResponse,
                thumbnailResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(buildingService.getBuilding(1L)).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/buildings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("테스트 빌딩"))
                .andExpect(jsonPath("$.description").value("테스트 빌딩 설명"))
                .andExpect(jsonPath("$.file.id").value(1L))
                .andExpect(jsonPath("$.file.fileUrl").value("https://example.com/buildings/1/file"))
                .andExpect(jsonPath("$.thumbnail.id").value(2L))
                .andExpect(jsonPath("$.thumbnail.fileUrl").value("https://example.com/buildings/1/thumbnail"));
        
        verify(buildingService).getBuilding(1L);
    }

    @Test
    @DisplayName("모든 빌딩 조회 성공 테스트")
    void getAllBuildingsSuccess() throws Exception {
        FileResponse fileResponse1 = new FileResponse(
                1L,
                "buildings/1/file",
                "test1.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/buildings/1/file",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse1 = new FileResponse(
                2L,
                "buildings/1/thumbnail",
                "thumbnail1.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/buildings/1/thumbnail",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        BuildingResponse response1 = new BuildingResponse(
                1L,
                "테스트 빌딩 1",
                "테스트 빌딩 설명 1",
                fileResponse1,
                thumbnailResponse1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse fileResponse2 = new FileResponse(
                3L,
                "buildings/2/file",
                "test2.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/buildings/2/file",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse2 = new FileResponse(
                4L,
                "buildings/2/thumbnail",
                "thumbnail2.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/buildings/2/thumbnail",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        BuildingResponse response2 = new BuildingResponse(
                2L,
                "테스트 빌딩 2",
                "테스트 빌딩 설명 2",
                fileResponse2,
                thumbnailResponse2,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(buildingService.getAllBuildings()).willReturn(List.of(response1, response2));

        mockMvc.perform(MockMvcRequestBuilders.get("/buildings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("테스트 빌딩 1"))
                .andExpect(jsonPath("$[0].file.id").value(1L))
                .andExpect(jsonPath("$[0].thumbnail.id").value(2L))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("테스트 빌딩 2"))
                .andExpect(jsonPath("$[1].file.id").value(3L))
                .andExpect(jsonPath("$[1].thumbnail.id").value(4L));
        
        verify(buildingService).getAllBuildings();
    }

    @Test
    @DisplayName("빌딩 업데이트 성공 테스트")
    void updateBuildingSuccess() throws Exception {
        BuildingUpdateRequest request = BuildingUpdateRequest.of("업데이트된 빌딩", "업데이트된 설명", 3L, 4L);
        
        FileResponse fileResponse = new FileResponse(
                3L,
                "buildings/1/file",
                "updated.obj",
                "application/octet-stream",
                FileType.DRAWING,
                FileStatus.COMPLETE,
                "https://example.com/buildings/1/file/updated",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        FileResponse thumbnailResponse = new FileResponse(
                4L,
                "buildings/1/thumbnail",
                "updated_thumbnail.png",
                "image/png",
                FileType.THUMBNAIL,
                FileStatus.COMPLETE,
                "https://example.com/buildings/1/thumbnail/updated",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        
        BuildingResponse response = new BuildingResponse(
                1L,
                "업데이트된 빌딩",
                "업데이트된 설명",
                fileResponse,
                thumbnailResponse,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(buildingService.updateBuilding(anyLong(), any(BuildingUpdateRequest.class))).willReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/buildings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("업데이트된 빌딩"))
                .andExpect(jsonPath("$.description").value("업데이트된 설명"))
                .andExpect(jsonPath("$.file.id").value(3L))
                .andExpect(jsonPath("$.file.fileUrl").value("https://example.com/buildings/1/file/updated"))
                .andExpect(jsonPath("$.thumbnail.id").value(4L))
                .andExpect(jsonPath("$.thumbnail.fileUrl").value("https://example.com/buildings/1/thumbnail/updated"));
        
        verify(buildingService).updateBuilding(anyLong(), any(BuildingUpdateRequest.class));
    }

    @Test
    @DisplayName("빌딩 삭제 성공 테스트")
    void deleteBuildingSuccess() throws Exception {
        doNothing().when(buildingService).deleteBuilding(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/buildings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        
        verify(buildingService).deleteBuilding(1L);
    }
} 