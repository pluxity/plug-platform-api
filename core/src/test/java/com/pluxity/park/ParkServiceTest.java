package com.pluxity.park;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityService;
import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.park.dto.ParkCreateRequest;
import com.pluxity.park.dto.ParkResponse;
import com.pluxity.park.dto.ParkUpdateRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@Transactional
class ParkServiceTest {

    @Autowired
    private ParkService parkService;

    @Autowired
    private ParkRepository parkRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private FacilityService facilityService;

    private ParkCreateRequest createRequest;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));

        // MockMultipartFile 생성
        MultipartFile drawingFile = new MockMultipartFile("drawing.png", "drawing.png", "image/png", fileContent);
        MultipartFile thumbnailFile = new MockMultipartFile("thumbnail.png", "thumbnail.png", "image/png", fileContent);

        // 파일 업로드 초기화
        Long drawingFileId = fileService.initiateUpload(drawingFile);
        Long thumbnailFileId = fileService.initiateUpload(thumbnailFile);

        // 테스트 데이터 준비
        FacilityCreateRequest facilityRequest = new FacilityCreateRequest(
                "테스트 공원",
                "PARK-001",
                "테스트 공원 설명입니다.",
                drawingFileId,
                thumbnailFileId
                ,null
                ,null
                ,null
        );

        // 경계 정보 (GeoJSON 형식의 문자열 예시)
        String boundaryJson = "{\"type\":\"Polygon\",\"coordinates\":[[[127.0,37.5],[127.1,37.5],[127.1,37.6],[127.0,37.6],[127.0,37.5]]]}";

        createRequest = new ParkCreateRequest(
                facilityRequest,
                boundaryJson
        );
    }

    @Test
    @DisplayName("유효한 요청으로 공원 생성 시 공원과 경계 정보가 저장된다")
    void save_WithValidRequest_SavesParkAndBoundary() {
        // when
        Long id = parkService.save(createRequest);

        // then
        assertThat(id).isNotNull();

        // 저장된 공원 확인
        ParkResponse savedPark = parkService.findById(id);
        assertThat(savedPark).isNotNull();
        assertThat(savedPark.facility().name()).isEqualTo("테스트 공원");
        assertThat(savedPark.facility().description()).isEqualTo("테스트 공원 설명입니다.");
        assertThat(savedPark.boundary()).isEqualTo(createRequest.boundary());
    }

    @Test
    @DisplayName("모든 공원 조회 시 공원 목록이 반환된다")
    void findAll_ReturnsListOfParkResponses() {
        // given
        parkService.save(createRequest);

        // when
        List<ParkResponse> responses = parkService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.get(0).facility().name()).isEqualTo("테스트 공원");
        assertThat(responses.get(0).facility().description()).isEqualTo("테스트 공원 설명입니다.");
    }

    @Test
    @DisplayName("ID로 공원 조회 시 공원 정보가 반환된다")
    void findById_WithExistingId_ReturnsParkResponse() {
        // given
        Long id = parkService.save(createRequest);

        // when
        ParkResponse response = parkService.findById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.facility().name()).isEqualTo("테스트 공원");
        assertThat(response.boundary()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 공원 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        // CustomException에 NOT_FOUND_PARK 에러 코드가 정의되어 있어야 합니다.
        assertThrows(CustomException.class, () -> parkService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 공원 정보 수정 시 공원 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesPark() {
        // given
        Long id = parkService.save(createRequest);

        String updatedBoundary = "{\"type\":\"Polygon\",\"coordinates\":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]}";

        ParkUpdateRequest updateRequest = new ParkUpdateRequest(
                new FacilityUpdateRequest(
                        "수정된 공원 이름",
                        "PARK-002",
                        "수정된 공원 설명",
                        null
                        ,null
                        ,null
                        ,null
                ),
                updatedBoundary
        );

        // when
        parkService.update(id, updateRequest);

        // then
        Facility updatedFacility = facilityService.findById(id);
        Park updatedPark = parkRepository.findById(id).orElseThrow();

        assertThat(updatedFacility.getName()).isEqualTo("수정된 공원 이름");
        assertThat(updatedFacility.getDescription()).isEqualTo("수정된 공원 설명");
        assertThat(updatedPark.getBoundary()).isEqualTo(updatedBoundary);
    }

    @Test
    @DisplayName("공원 삭제 시 연관된 시설 정보와 함께 삭제된다")
    void delete_RemovesParkAndFacility() {
        // given
        Long id = parkService.save(createRequest);

        // when
        ParkResponse response = parkService.findById(id);
        assertThat(response).isNotNull();

        // then
        parkService.delete(id);

        // 삭제 후에는 해당 ID로 공원을 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> parkService.findById(id));
        // 연관된 시설 정보도 함께 삭제되었는지 확인
        assertThrows(CustomException.class, () -> facilityService.findById(id));
    }
}