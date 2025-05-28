package com.pluxity.facility.service;

import com.pluxity.facility.building.*;
import com.pluxity.facility.building.dto.BuildingCreateRequest;
import com.pluxity.facility.building.dto.BuildingResponse;
import com.pluxity.facility.building.dto.BuildingUpdateRequest;
import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BuildingServiceTest {

    @Autowired
    BuildingService buildingService;

    @Autowired
    BuildingRepository buildingRepository;

    @Autowired
    FileService fileService;

    @Autowired
    FacilityService facilityService;


    private Long drawingFileId;
    private Long thumbnailFileId;
    private BuildingCreateRequest createRequest;
    private Building building;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));
        
        // MockMultipartFile 생성
        MultipartFile drawingFile = new MockMultipartFile(
                "drawing.png", "drawing.png", "image/png", fileContent);
        MultipartFile thumbnailFile = new MockMultipartFile(
                "thumbnail.png", "thumbnail.png", "image/png", fileContent);
        
        // 파일 업로드 초기화
        drawingFileId = fileService.initiateUpload(drawingFile);
        thumbnailFileId = fileService.initiateUpload(thumbnailFile);
        
        // 테스트 데이터 준비
        FacilityCreateRequest facilityRequest = new FacilityCreateRequest(
                "테스트 건물",
                "AAA",
                "테스트 건물 설명",
                drawingFileId,
                thumbnailFileId
        );
        
        List<FloorRequest> floorRequests = new ArrayList<>();
        floorRequests.add(new FloorRequest(
                "1층", 
                1
        ));
        
        createRequest = new BuildingCreateRequest(
                facilityRequest,
                floorRequests
        );
    }

    @Test
    @DisplayName("유효한 요청으로 건물 생성 시 건물과 층이 저장된다")
    void save_WithValidRequest_SavesBuildingAndFloors() {
        // when
        Long id = buildingService.save(createRequest);

        // then
        assertThat(id).isNotNull();
        
        // 저장된 건물 확인
        BuildingResponse savedBuilding = buildingService.findById(id);
        assertThat(savedBuilding).isNotNull();
        assertThat(savedBuilding.facility().name()).isEqualTo("테스트 건물");
        assertThat(savedBuilding.facility().description()).isEqualTo("테스트 건물 설명");
        assertThat(savedBuilding.floors()).isNotEmpty();
    }

    @Test
    @DisplayName("모든 건물 조회 시 건물 목록이 반환된다")
    void findAll_ReturnsListOfBuildingResponses() {
        // given
        Long id = buildingService.save(createRequest);
        
        // when
        List<BuildingResponse> responses = buildingService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.getFirst().facility().name()).isEqualTo("테스트 건물");
        assertThat(responses.getFirst().facility().description()).isEqualTo("테스트 건물 설명");
    }

    @Test
    @DisplayName("ID로 건물 조회 시 건물 정보가 반환된다")
    void findById_WithExistingId_ReturnsBuildingResponse() {
        // given
        Long id = buildingService.save(createRequest);

        // when
        BuildingResponse response = buildingService.findById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.facility().name()).isEqualTo("테스트 건물");
        assertThat(response.facility().description()).isEqualTo("테스트 건물 설명");
        assertThat(response.floors()).isNotEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 건물 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> buildingService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 건물 정보 수정 시 건물 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesBuilding() {
        // given
        Long id = buildingService.save(createRequest);
        BuildingUpdateRequest updateRequest = new BuildingUpdateRequest(
                "수정된 건물",
                "수정된 건물 설명",
                drawingFileId,
                thumbnailFileId
        );

        // when
        buildingService.update(id, updateRequest);

        // then
        BuildingResponse updatedBuilding = buildingService.findById(id);
        assertThat(updatedBuilding.facility().name()).isEqualTo("수정된 건물");
        assertThat(updatedBuilding.facility().description()).isEqualTo("수정된 건물 설명");
    }

    @Test
    @DisplayName("건물 삭제 시 모든 이력이 삭제된다")
    void delete() {
        // given
        Long id = buildingService.save(createRequest);
        
        // when
        BuildingResponse response = buildingService.findById(id);
        assertThat(response).isNotNull();
        
        // then
        buildingService.delete(id);
        
        // 삭제 후에는 해당 ID로 건물을 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> buildingService.findById(id));
    }

}