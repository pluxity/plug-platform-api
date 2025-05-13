package com.pluxity.icon.service;

import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.icon.dto.IconCreateRequest;
import com.pluxity.icon.dto.IconResponse;
import com.pluxity.icon.dto.IconUpdateRequest;
import com.pluxity.icon.entity.Icon;
import com.pluxity.icon.repository.IconRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class IconServiceTest {

    @Autowired
    private IconRepository iconRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private IconService iconService;

    private Long fileId;

    @BeforeEach
    void setup() throws IOException {
        // 테스트 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));

        // MockMultipartFile 생성
        MultipartFile multipartFile = new MockMultipartFile(
                "temp.png",  // 파일명
                "temp.png",  // 원본 파일명
                "image/png", // 컨텐츠 타입
                fileContent  // 파일 내용
        );

        // FileService를 통해 파일 업로드 초기화
        fileId = fileService.initiateUpload(multipartFile);
    }

    @Test
    @DisplayName("유효한 요청으로 아이콘 생성 시 아이콘과 관련 파일이 저장된다")
    void createIcon_WithValidRequest_SavesIconAndFinalizeFile() {
        // given
        IconCreateRequest request = new IconCreateRequest(
                "테스트 아이콘",
                "테스트 아이콘 설명",
                fileId
        );

        // when
        Long savedId = iconService.createIcon(request);

        // then
        assertThat(savedId).isNotNull();
        
        // 저장된 아이콘 확인
        IconResponse savedIcon = iconService.getIcon(savedId);
        assertThat(savedIcon).isNotNull();
        assertThat(savedIcon.name()).isEqualTo("테스트 아이콘");
        assertThat(savedIcon.file()).isNotNull();
    }

    @Test
    @DisplayName("파일 ID가 없는 요청으로 아이콘 생성 시 아이콘만 저장된다")
    void createIcon_WithoutFileId_SavesOnlyIcon() {
        // given
        IconCreateRequest request = new IconCreateRequest(
                "테스트 아이콘",
                "테스트 아이콘 설명",
                null
        );

        // when
        Long savedId = iconService.createIcon(request);

        // then
        assertThat(savedId).isNotNull();
        
        // 저장된 아이콘 확인
        IconResponse savedIcon = iconService.getIcon(savedId);
        assertThat(savedIcon).isNotNull();
        assertThat(savedIcon.name()).isEqualTo("테스트 아이콘");
        assertThat(savedIcon.file()).isNull();
    }

    @Test
    @DisplayName("존재하는 ID로 아이콘 조회 시 아이콘 정보가 반환된다")
    void getIcon_WithExistingId_ReturnsIconResponse() {
        // given
        Icon icon = Icon.builder()
                .name("테스트 아이콘")
                .build();
        Icon savedIcon = iconRepository.save(icon);

        // when
        IconResponse response = iconService.getIcon(savedIcon.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedIcon.getId());
        assertThat(response.name()).isEqualTo("테스트 아이콘");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 아이콘 조회 시 예외가 발생한다")
    void getIcon_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 999L;

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> iconService.getIcon(nonExistingId));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("모든 아이콘 조회 시 아이콘 목록이 반환된다")
    void getIcons_ReturnsListOfIconResponses() {
        // given
        Icon icon1 = Icon.builder()
                .name("아이콘 1")
                .build();

        Icon icon2 = Icon.builder()
                .name("아이콘 2")
                .build();

        iconRepository.save(icon1);
        iconRepository.save(icon2);

        // when
        List<IconResponse> responses = iconService.getIcons();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("name")
                .contains("아이콘 1", "아이콘 2");
    }

    @Test
    @DisplayName("유효한 요청으로 아이콘 수정 시 아이콘 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesIcon() {
        // given
        Icon icon = Icon.builder()
                .name("원본 아이콘")
                .build();
        Icon savedIcon = iconRepository.save(icon);

        IconUpdateRequest request = new IconUpdateRequest(
                "수정된 아이콘",
                "수정된 아이콘 설명",
                null
        );

        // when
        iconService.update(savedIcon.getId(), request);

        // then
        IconResponse updatedIcon = iconService.getIcon(savedIcon.getId());
        assertThat(updatedIcon).isNotNull();
        assertThat(updatedIcon.name()).isEqualTo("수정된 아이콘");
    }

    @Test
    @DisplayName("파일 ID가 포함된 요청으로 아이콘 수정 시 아이콘 정보와 파일 정보가 업데이트된다")
    void update_WithFileId_UpdatesIconAndFileId() {
        // given
        Icon icon = Icon.builder()
                .name("원본 아이콘")
                .build();
        Icon savedIcon = iconRepository.save(icon);

        IconUpdateRequest request = new IconUpdateRequest(
                "수정된 아이콘",
                "수정된 아이콘 설명",
                fileId
        );

        // when
        iconService.update(savedIcon.getId(), request);

        // then
        IconResponse updatedIcon = iconService.getIcon(savedIcon.getId());
        assertThat(updatedIcon).isNotNull();
        assertThat(updatedIcon.name()).isEqualTo("수정된 아이콘");
        assertThat(updatedIcon.file()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 아이콘 수정 시 예외가 발생한다")
    void update_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 999L;
        IconUpdateRequest request = new IconUpdateRequest(
                "수정된 아이콘",
                "수정된 아이콘 설명",
                null
        );

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> iconService.update(nonExistingId, request));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("아이콘 삭제 시 아이콘이 삭제된다")
    void delete_DeletesIcon() {
        // given
        Icon icon = Icon.builder()
                .name("테스트 아이콘")
                .build();
        Icon savedIcon = iconRepository.save(icon);

        // when
        iconService.delete(savedIcon.getId());

        // then
        assertThat(iconRepository.findById(savedIcon.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 아이콘 삭제 시 예외가 발생한다")
    void delete_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 999L;

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> iconService.delete(nonExistingId));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}