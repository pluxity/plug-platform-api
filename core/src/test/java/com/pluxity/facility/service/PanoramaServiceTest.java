package com.pluxity.facility.service;

import com.pluxity.CoreApplicationTest;
import com.pluxity.facility.dto.*;
import com.pluxity.facility.entity.Panorama;
import com.pluxity.facility.repository.PanoramaRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PanoramaServiceTest extends CoreApplicationTest {

    @Autowired
    private PanoramaService panoramaService;

    @Autowired
    private PanoramaRepository panoramaRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private FacilityService facilityService;

    @Test
    @DisplayName("유효한 요청으로 파노라마 생성 시 파노라마가 저장된다")
    void save_WithValidRequest_SavesPanorama() {
        // given
        PanoramaCreateRequest request = createPanoramaRequest();

        // when
        Long id = panoramaService.save(request);

        // then
        assertThat(id).isNotNull();
        
        // 저장된 파노라마 확인
        Panorama savedPanorama = (Panorama) facilityService.findById(id);
        assertThat(savedPanorama).isNotNull();
        assertThat(savedPanorama.getName()).isEqualTo("테스트 파노라마");
        assertThat(savedPanorama.getDescription()).isEqualTo("테스트 파노라마 설명");
        // 위치 정보는 LocationStrategy를 통해 저장되므로 직접 검증하지 않음
    }

    @Test
    @DisplayName("모든 파노라마 조회 시 파노라마 목록이 반환된다")
    void findAll_ReturnsListOfPanoramaResponses() {
        // given
        PanoramaCreateRequest request = createPanoramaRequest();
        Long id = panoramaService.save(request);
        
        // when
        List<PanoramaResponse> responses = panoramaService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        // 적어도 하나의 파노라마가 테스트 파노라마와 일치하는지 확인
        boolean foundTestPanorama = responses.stream()
                .anyMatch(panorama -> 
                        panorama.facility().name().equals("테스트 파노라마"));
        assertThat(foundTestPanorama).isTrue();
    }

    @Test
    @DisplayName("ID로 파노라마 조회 시 파노라마 정보가 반환된다")
    void findById_WithExistingId_ReturnsPanoramaResponse() {
        // given
        PanoramaCreateRequest request = createPanoramaRequest();
        Long id = panoramaService.save(request);

        // when
        PanoramaResponse response = panoramaService.findById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.facility().name()).isEqualTo("테스트 파노라마");
        assertThat(response.facility().description()).isEqualTo("테스트 파노라마 설명");
        // 위치 정보는 response.location에서 접근 가능
    }

    @Test
    @DisplayName("존재하지 않는 ID로 파노라마 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = DEFAULT_NON_EXISTING_ID;

        // when & then
        assertThrows(CustomException.class, () -> panoramaService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 파노라마 정보 수정 시 파노라마 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesPanorama() {
        // given
        PanoramaCreateRequest createRequest = createPanoramaRequest();
        Long id = panoramaService.save(createRequest);
        
        PanoramaUpdateRequest updateRequest = createPanoramaUpdateRequest();

        // when
        panoramaService.update(id, updateRequest);

        // then
        PanoramaResponse updatedPanorama = panoramaService.findById(id);
        assertThat(updatedPanorama.facility().name()).isEqualTo("수정된 파노라마");
        assertThat(updatedPanorama.facility().description()).isEqualTo("수정된 파노라마 설명");
    }

    @Test
    @DisplayName("파노라마 삭제 시 파노라마가 삭제된다")
    void delete_DeletesPanorama() {
        // given
        PanoramaCreateRequest request = createPanoramaRequest();
        Long id = panoramaService.save(request);

        // when
        panoramaService.delete(id);

        // then
        assertThrows(CustomException.class, () -> panoramaService.findById(id));
    }
} 