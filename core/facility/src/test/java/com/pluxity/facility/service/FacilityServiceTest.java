package com.pluxity.facility.service;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityResponse;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.repository.FacilityRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private FacilityService facilityService;

    private Facility facility;
    private FileEntity fileEntity;
    private FileEntity thumbnailEntity;
    private FacilityCreateRequest createRequest;
    private FacilityUpdateRequest updateRequest;
    private FileResponse fileResponse;
    private FileResponse thumbnailResponse;

    @BeforeEach
    void setUp() throws Exception {
        facility = Facility.builder()
                .name("테스트 시설")
                .description("테스트 시설 설명")
                .fileId(1L)
                .thumbnailId(2L)
                .build();
        
        try {
            java.lang.reflect.Field idField = Facility.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(facility, 1L);
            
            java.lang.reflect.Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(facility, LocalDateTime.now());
            
            java.lang.reflect.Field updatedByField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedByField.setAccessible(true);
            updatedByField.set(facility, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }

        fileEntity = FileEntity.builder()
                .filePath("facilities/1/file")
                .originalFileName("test.obj")
                .contentType("application/octet-stream")
                .fileType(FileType.DRAWING)
                .build();

        fileEntity.makeComplete("facilities/1/file");
        
        thumbnailEntity = FileEntity.builder()
                .filePath("facilities/1/thumbnail")
                .originalFileName("thumbnail.png")
                .contentType("image/png")
                .fileType(FileType.THUMBNAIL)
                .build();

        thumbnailEntity.makeComplete("facilities/1/thumbnail");
        
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

        createRequest = FacilityCreateRequest.of("테스트 시설", "테스트 시설 설명", 1L, 2L);
        updateRequest = FacilityUpdateRequest.of("업데이트된 시설", "업데이트된 설명", 3L, 4L);
        
        fileResponse = FileResponse.from(fileEntity);
        thumbnailResponse = FileResponse.from(thumbnailEntity);
    }

    @Test
    @DisplayName("시설 생성 성공 테스트")
    void createFacilitySuccess() {
        given(facilityRepository.save(any(Facility.class))).willReturn(facility);
        given(fileService.finalizeUpload(1L, "facilities/1/file")).willReturn(fileEntity);
        given(fileService.finalizeUpload(2L, "facilities/1/thumbnail")).willReturn(thumbnailEntity);
        given(fileService.getFile(1L)).willReturn(fileEntity);
        given(fileService.getFile(2L)).willReturn(thumbnailEntity);

        FacilityResponse response = facilityService.createFacility(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("테스트 시설");
        assertThat(response.description()).isEqualTo("테스트 시설 설명");
        assertThat(response.file()).isNotNull();
        assertThat(response.file().id()).isEqualTo(1L);
        assertThat(response.thumbnail()).isNotNull();
        assertThat(response.thumbnail().id()).isEqualTo(2L);
        
        verify(facilityRepository).save(any(Facility.class));
        verify(fileService).finalizeUpload(1L, "facilities/1/file");
        verify(fileService).finalizeUpload(2L, "facilities/1/thumbnail");
    }

    @Test
    @DisplayName("시설 조회 성공 테스트")
    void getFacilitySuccess() {
        given(facilityRepository.findById(1L)).willReturn(Optional.of(facility));
        given(fileService.getFile(1L)).willReturn(fileEntity);
        given(fileService.getFile(2L)).willReturn(thumbnailEntity);

        FacilityResponse response = facilityService.getFacility(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("테스트 시설");
        assertThat(response.description()).isEqualTo("테스트 시설 설명");
        assertThat(response.file()).isNotNull();
        assertThat(response.file().id()).isEqualTo(1L);
        assertThat(response.thumbnail()).isNotNull();
        assertThat(response.thumbnail().id()).isEqualTo(2L);
        
        verify(facilityRepository).findById(1L);
        verify(fileService).getFile(1L);
        verify(fileService).getFile(2L);
    }

    @Test
    @DisplayName("존재하지 않는 시설 조회 실패 테스트")
    void getFacilityNotFound() {
        given(facilityRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> facilityService.getFacility(999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
        
        verify(facilityRepository).findById(999L);
    }

    @Test
    @DisplayName("모든 시설 조회 테스트")
    void getAllFacilitysSuccess() {
        given(facilityRepository.findAll()).willReturn(List.of(facility));
        given(fileService.getFile(1L)).willReturn(fileEntity);
        given(fileService.getFile(2L)).willReturn(thumbnailEntity);

        List<FacilityResponse> responses = facilityService.getAllFacilitys();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().id()).isEqualTo(1L);
        assertThat(responses.getFirst().name()).isEqualTo("테스트 시설");
        assertThat(responses.getFirst().description()).isEqualTo("테스트 시설 설명");
        assertThat(responses.getFirst().file()).isNotNull();
        assertThat(responses.getFirst().file().id()).isEqualTo(1L);
        assertThat(responses.getFirst().thumbnail()).isNotNull();
        assertThat(responses.getFirst().thumbnail().id()).isEqualTo(2L);
        
        verify(facilityRepository).findAll();
        verify(fileService).getFile(1L);
        verify(fileService).getFile(2L);
    }

    @Test
    @DisplayName("시설 업데이트 성공 테스트")
    void updateFacilitySuccess() {
        Facility updatedFacility = Facility.builder()
                .name("업데이트된 시설")
                .description("업데이트된 설명")
                .fileId(3L)
                .thumbnailId(4L)
                .build();
        
        FileEntity updatedFileEntity = FileEntity.builder()
                .filePath("facilities/1/file")
                .originalFileName("updated.obj")
                .contentType("application/octet-stream")
                .fileType(FileType.DRAWING)
                .build();

        updatedFileEntity.makeComplete("facilities/1/file");
                
        FileEntity updatedThumbnailEntity = FileEntity.builder()
                .filePath("facilities/1/thumbnail")
                .originalFileName("updated_thumbnail.png")
                .contentType("image/png")
                .fileType(FileType.THUMBNAIL)
                .build();

        updatedThumbnailEntity.makeComplete("facilities/1/thumbnail");
        
        try {
            java.lang.reflect.Field idField = Facility.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(updatedFacility, 1L);
            
            java.lang.reflect.Field createdAtField = BaseEntity.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(updatedFacility, LocalDateTime.now());
            
            java.lang.reflect.Field updatedByField = BaseEntity.class.getDeclaredField("updatedAt");
            updatedByField.setAccessible(true);
            updatedByField.set(updatedFacility, LocalDateTime.now());
            
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
        
        given(facilityRepository.findById(1L)).willReturn(Optional.of(facility));
        given(fileService.finalizeUpload(3L, "facilities/1/file")).willReturn(updatedFileEntity);
        given(fileService.finalizeUpload(4L, "facilities/1/thumbnail")).willReturn(updatedThumbnailEntity);
        given(facilityRepository.save(any(Facility.class))).willReturn(updatedFacility);
        given(fileService.getFile(3L)).willReturn(updatedFileEntity);
        given(fileService.getFile(4L)).willReturn(updatedThumbnailEntity);

        FacilityResponse response = facilityService.updateFacility(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("업데이트된 시설");
        assertThat(response.description()).isEqualTo("업데이트된 설명");
        assertThat(response.file()).isNotNull();
        assertThat(response.file().id()).isEqualTo(3L);
        assertThat(response.thumbnail()).isNotNull();
        assertThat(response.thumbnail().id()).isEqualTo(4L);
        
        verify(facilityRepository).findById(1L);
        verify(fileService).finalizeUpload(3L, "facilities/1/file");
        verify(fileService).finalizeUpload(4L, "facilities/1/thumbnail");
        verify(facilityRepository).save(any(Facility.class));
    }

    @Test
    @DisplayName("시설 삭제 성공 테스트")
    void deleteFacilitySuccess() {
        given(facilityRepository.findById(1L)).willReturn(Optional.of(facility));

        facilityService.deleteFacility(1L);

        verify(facilityRepository).findById(1L);
        verify(facilityRepository).delete(facility);
    }
} 