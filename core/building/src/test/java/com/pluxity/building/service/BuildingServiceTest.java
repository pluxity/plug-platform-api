package com.pluxity.building.service;

import com.pluxity.building.dto.BuildingCreateRequest;
import com.pluxity.building.dto.BuildingResponse;
import com.pluxity.building.dto.BuildingUpdateRequest;
import com.pluxity.building.entity.Building;
import com.pluxity.building.repository.BuildingRepository;
import com.pluxity.file.constant.FileStatus;
import com.pluxity.file.constant.FileType;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.service.FileService;
import com.pluxity.global.entity.BaseEntity;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private BuildingService buildingService;

    private Building building;
    private FileEntity fileEntity;
    private FileEntity thumbnailEntity;
    private BuildingCreateRequest createRequest;
    private BuildingUpdateRequest updateRequest;
    private FileResponse fileResponse;
    private FileResponse thumbnailResponse;

    @BeforeEach
    void setUp() throws Exception {
        building = Building.builder()
                .name("테스트 빌딩")
                .description("테스트 빌딩 설명")
                .fileId(1L)
                .thumbnailId(2L)
                .build();
        
        try {
            java.lang.reflect.Field idField = Building.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(building, 1L);
            
            java.lang.reflect.Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(building, LocalDateTime.now());
            
            java.lang.reflect.Field updatedByField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedByField.setAccessible(true);
            updatedByField.set(building, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }

        fileEntity = FileEntity.builder()
                .filePath("buildings/1/file")
                .originalFileName("test.obj")
                .contentType("application/octet-stream")
                .fileType(FileType.DRAWING)
                .build();

        fileEntity.makeComplete("buildings/1/file");
        
        thumbnailEntity = FileEntity.builder()
                .filePath("buildings/1/thumbnail")
                .originalFileName("thumbnail.png")
                .contentType("image/png")
                .fileType(FileType.THUMBNAIL)
                .build();

        thumbnailEntity.makeComplete("buildings/1/thumbnail");
        
        java.lang.reflect.Field fileIdField = FileEntity.class.getDeclaredField("id");
        fileIdField.setAccessible(true);
        fileIdField.set(fileEntity, 1L);
        fileIdField.set(thumbnailEntity, 2L);

        java.lang.reflect.Field fileCreatedAtField = BaseEntity.class.getDeclaredField("createdAt");
        fileCreatedAtField.setAccessible(true);
        fileCreatedAtField.set(fileEntity, LocalDateTime.now());
        fileCreatedAtField.set(thumbnailEntity, LocalDateTime.now());

        java.lang.reflect.Field fileUpdatedAtField = BaseEntity.class.getDeclaredField("updatedAt");
        fileUpdatedAtField.setAccessible(true);
        fileUpdatedAtField.set(fileEntity, LocalDateTime.now());
        fileUpdatedAtField.set(thumbnailEntity, LocalDateTime.now());

        createRequest = BuildingCreateRequest.of("테스트 빌딩", "테스트 빌딩 설명", 1L, 2L);
        updateRequest = BuildingUpdateRequest.of("업데이트된 빌딩", "업데이트된 설명", 3L, 4L);
        
        fileResponse = FileResponse.from(fileEntity);
        thumbnailResponse = FileResponse.from(thumbnailEntity);
    }

    @Test
    @DisplayName("빌딩 생성 성공 테스트")
    void createBuildingSuccess() {
        given(buildingRepository.save(any(Building.class))).willReturn(building);
        given(fileService.finalizeUpload(1L, "buildings/1/file")).willReturn(fileEntity);
        given(fileService.finalizeUpload(2L, "buildings/1/thumbnail")).willReturn(thumbnailEntity);
        given(fileService.getFile(1L)).willReturn(fileEntity);
        given(fileService.getFile(2L)).willReturn(thumbnailEntity);

        BuildingResponse response = buildingService.createBuilding(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("테스트 빌딩");
        assertThat(response.description()).isEqualTo("테스트 빌딩 설명");
        assertThat(response.file()).isNotNull();
        assertThat(response.file().id()).isEqualTo(1L);
        assertThat(response.thumbnail()).isNotNull();
        assertThat(response.thumbnail().id()).isEqualTo(2L);
        
        verify(buildingRepository).save(any(Building.class));
        verify(fileService).finalizeUpload(1L, "buildings/1/file");
        verify(fileService).finalizeUpload(2L, "buildings/1/thumbnail");
    }

    @Test
    @DisplayName("빌딩 조회 성공 테스트")
    void getBuildingSuccess() {
        given(buildingRepository.findById(1L)).willReturn(Optional.of(building));
        given(fileService.getFile(1L)).willReturn(fileEntity);
        given(fileService.getFile(2L)).willReturn(thumbnailEntity);

        BuildingResponse response = buildingService.getBuilding(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("테스트 빌딩");
        assertThat(response.description()).isEqualTo("테스트 빌딩 설명");
        assertThat(response.file()).isNotNull();
        assertThat(response.file().id()).isEqualTo(1L);
        assertThat(response.thumbnail()).isNotNull();
        assertThat(response.thumbnail().id()).isEqualTo(2L);
        
        verify(buildingRepository).findById(1L);
        verify(fileService).getFile(1L);
        verify(fileService).getFile(2L);
    }

    @Test
    @DisplayName("존재하지 않는 빌딩 조회 실패 테스트")
    void getBuildingNotFound() {
        given(buildingRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> buildingService.getBuilding(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        
        verify(buildingRepository).findById(999L);
    }

    @Test
    @DisplayName("모든 빌딩 조회 테스트")
    void getAllBuildingsSuccess() {
        given(buildingRepository.findAll()).willReturn(List.of(building));
        given(fileService.getFile(1L)).willReturn(fileEntity);
        given(fileService.getFile(2L)).willReturn(thumbnailEntity);

        List<BuildingResponse> responses = buildingService.getAllBuildings();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(1L);
        assertThat(responses.getFirst().name()).isEqualTo("테스트 빌딩");
        assertThat(responses.getFirst().description()).isEqualTo("테스트 빌딩 설명");
        assertThat(responses.getFirst().file()).isNotNull();
        assertThat(responses.getFirst().file().id()).isEqualTo(1L);
        assertThat(responses.getFirst().thumbnail()).isNotNull();
        assertThat(responses.getFirst().thumbnail().id()).isEqualTo(2L);
        
        verify(buildingRepository).findAll();
        verify(fileService).getFile(1L);
        verify(fileService).getFile(2L);
    }

    @Test
    @DisplayName("빌딩 업데이트 성공 테스트")
    void updateBuildingSuccess() {
        Building updatedBuilding = Building.builder()
                .name("업데이트된 빌딩")
                .description("업데이트된 설명")
                .fileId(3L)
                .thumbnailId(4L)
                .build();
        
        FileEntity updatedFileEntity = FileEntity.builder()
                .filePath("buildings/1/file")
                .originalFileName("updated.obj")
                .contentType("application/octet-stream")
                .fileType(FileType.DRAWING)
                .build();

        updatedFileEntity.makeComplete("buildings/1/file");
                
        FileEntity updatedThumbnailEntity = FileEntity.builder()
                .filePath("buildings/1/thumbnail")
                .originalFileName("updated_thumbnail.png")
                .contentType("image/png")
                .fileType(FileType.THUMBNAIL)
                .build();

        updatedThumbnailEntity.makeComplete("buildings/1/thumbnail");
        
        try {
            java.lang.reflect.Field idField = Building.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(updatedBuilding, 1L);
            
            java.lang.reflect.Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(updatedBuilding, LocalDateTime.now());
            
            java.lang.reflect.Field updatedByField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedByField.setAccessible(true);
            updatedByField.set(updatedBuilding, LocalDateTime.now());
            
            idField = FileEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(updatedFileEntity, 3L);
            idField.set(updatedThumbnailEntity, 4L);
            
            createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(updatedFileEntity, LocalDateTime.now());
            createdAtField.set(updatedThumbnailEntity, LocalDateTime.now());
            
            updatedByField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedByField.setAccessible(true);
            updatedByField.set(updatedFileEntity, LocalDateTime.now());
            updatedByField.set(updatedThumbnailEntity, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        given(buildingRepository.findById(1L)).willReturn(Optional.of(building));
        given(fileService.finalizeUpload(3L, "buildings/1/file")).willReturn(updatedFileEntity);
        given(fileService.finalizeUpload(4L, "buildings/1/thumbnail")).willReturn(updatedThumbnailEntity);
        given(buildingRepository.save(any(Building.class))).willReturn(updatedBuilding);
        given(fileService.getFile(3L)).willReturn(updatedFileEntity);
        given(fileService.getFile(4L)).willReturn(updatedThumbnailEntity);

        BuildingResponse response = buildingService.updateBuilding(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("업데이트된 빌딩");
        assertThat(response.description()).isEqualTo("업데이트된 설명");
        assertThat(response.file()).isNotNull();
        assertThat(response.file().id()).isEqualTo(3L);
        assertThat(response.thumbnail()).isNotNull();
        assertThat(response.thumbnail().id()).isEqualTo(4L);
        
        verify(buildingRepository).findById(1L);
        verify(fileService).finalizeUpload(3L, "buildings/1/file");
        verify(fileService).finalizeUpload(4L, "buildings/1/thumbnail");
        verify(buildingRepository).save(any(Building.class));
    }

    @Test
    @DisplayName("빌딩 삭제 성공 테스트")
    void deleteBuildingSuccess() {
        given(buildingRepository.findById(1L)).willReturn(Optional.of(building));

        buildingService.deleteBuilding(1L);

        verify(buildingRepository).findById(1L);
        verify(buildingRepository).delete(building);
    }
} 