package com.pluxity.file;

import static com.pluxity.global.constant.ErrorCode.INVALID_FILE_STATUS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.pluxity.file.constant.FileStatus;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.file.repository.FileRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.file.strategy.storage.FilePersistenceContext;
import com.pluxity.file.strategy.storage.FileProcessingContext;
import com.pluxity.file.strategy.storage.StorageStrategy;
import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.exception.CustomException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    // 실제 파일 시스템/S3 대신 가짜 객체로 대체하여 테스트
    @MockBean
    private StorageStrategy storageStrategy;

    private MockMultipartFile testFile;
    private String tempFilePath;

    @BeforeEach
    void setUp() throws Exception {
        // GIVEN: 모든 테스트에서 사용할 기본 MockMultipartFile 생성
        testFile = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test-file-content".getBytes()
        );

        // GIVEN: storageStrategy.save가 반환할 가상 경로 설정
        tempFilePath = "temp/" + UUID.randomUUID() + ".png";
        when(storageStrategy.save(any(FileProcessingContext.class))).thenReturn(tempFilePath);
    }

    // 테스트에서 임시 상태의 파일을 쉽게 만들기 위한 헬퍼 메서드
    private FileEntity createAndSaveTempFileEntity() {
        FileEntity tempFile = FileEntity.builder()
                .filePath("temp/some-temp-file.png")
                .originalFileName("temp.png")
                .contentType("image/png")
                .build(); // 초기 상태는 TEMP
        return fileRepository.save(tempFile);
    }

    @Test
    @DisplayName("파일 업로드 시작 시 임시 파일 엔티티를 생성하고 ID를 반환한다")
    void initiateUpload_withValidFile_createsTempFileEntityAndReturnsId() throws Exception {
        // WHEN
        Long fileId = fileService.initiateUpload(testFile);

        // THEN
        assertThat(fileId).isNotNull();

        // 1. StorageStrategy의 save 메소드가 정확히 1번 호출되었는지 검증
        verify(storageStrategy, times(1)).save(any(FileProcessingContext.class));

        // 2. DB에 파일 정보가 'TEMP' 상태로 저장되었는지 검증
        Optional<FileEntity> savedFileOpt = fileRepository.findById(fileId);
        assertThat(savedFileOpt).isPresent();
        FileEntity savedFile = savedFileOpt.get();

        assertThat(savedFile.getOriginalFileName()).isEqualTo("test.png");
        assertThat(savedFile.getContentType()).isEqualTo("image/png");
        assertThat(savedFile.getFilePath()).isEqualTo(tempFilePath); // Mock으로 설정한 경로
        assertThat(savedFile.getFileStatus()).isEqualTo(FileStatus.TEMP);
    }

    @Test
    @DisplayName("파일 업로드 중 I/O 오류 발생 시 CustomException을 던진다")
    void initiateUpload_whenIoErrorOccurs_throwsCustomException() throws IOException {
        // GIVEN: 파일 전송 시 IOException을 발생시키는 Mock 객체 생성
        MockMultipartFile failingFile = mock(MockMultipartFile.class);
        when(failingFile.getOriginalFilename()).thenReturn("failing.txt");
        doThrow(new IOException("Disk is full")).when(failingFile).transferTo(any(java.nio.file.Path.class));

        // WHEN & THEN
        CustomException exception = assertThrows(CustomException.class, () -> fileService.initiateUpload(failingFile));

        assertThat(exception.getMessage()).contains(ErrorCode.FAILED_TO_UPLOAD_FILE.getMessage());
    }

    @Test
    @DisplayName("임시 파일을 영구 저장 시 상태와 경로를 업데이트한다")
    void finalizeUpload_withTempFile_updatesStatusAndPath() throws Exception {
        // GIVEN: 임시 상태의 파일 엔티티를 미리 저장
        FileEntity tempFile = createAndSaveTempFileEntity();
        String permanentPath = "permanent/" + UUID.randomUUID() + ".png";
        when(storageStrategy.persist(any(FilePersistenceContext.class))).thenReturn(permanentPath);

        // WHEN
        FileEntity finalizedFile = fileService.finalizeUpload(tempFile.getId(), permanentPath);

        // THEN
        // 1. StorageStrategy의 persist 메소드가 1번 호출되었는지 검증
        try {
            verify(storageStrategy, times(1)).persist(any(FilePersistenceContext.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2. 반환된 엔티티와 DB의 상태가 'COMPLETE'로 변경되었는지 검증
        assertThat(finalizedFile.getFileStatus()).isEqualTo(FileStatus.COMPLETE);
        assertThat(finalizedFile.getFilePath()).isEqualTo(permanentPath);

        FileEntity updatedFile = fileRepository.findById(tempFile.getId()).orElseThrow();
        assertThat(updatedFile.getFileStatus()).isEqualTo(FileStatus.COMPLETE);
        assertThat(updatedFile.getFilePath()).isEqualTo(permanentPath);
    }

    @Test
    @DisplayName("영구 저장 시 파일 ID가 존재하지 않으면 예외를 던진다")
    void finalizeUpload_withNonExistentId_throwsCustomException() {
        // GIVEN
        Long nonExistentId = 9999L;

        // WHEN & THEN
        CustomException exception = assertThrows(CustomException.class, () -> fileService.finalizeUpload(nonExistentId, "some/path"));

        assertThat(exception.getErrorCode().getMessage()).isEqualTo(INVALID_FILE_STATUS.getMessage());
    }

    @Test
    @DisplayName("임시 상태가 아닌 파일을 영구 저장 시도 시 예외를 던진다")
    void finalizeUpload_withNonTempFile_throwsCustomException() {
        // GIVEN: 'COMPLETE' 상태의 파일을 생성
        FileEntity completeFile = createAndSaveTempFileEntity();
        completeFile.makeComplete("already/persisted.png");
        fileRepository.save(completeFile);

        // WHEN & THEN
        CustomException exception = assertThrows(CustomException.class, () -> fileService.finalizeUpload(completeFile.getId(), "new/path"));

    assertThat(exception.getMessage()).contains(ErrorCode.INVALID_FILE_STATUS.getMessage());
    }

    @Test
    @DisplayName("존재하는 파일 ID로 조회 시 FileEntity를 반환한다")
    void getFile_withExistingId_returnsFileEntity() {
        // GIVEN
        FileEntity savedFile = createAndSaveTempFileEntity();

        // WHEN
        FileEntity foundFile = fileService.getFile(savedFile.getId());

        // THEN
        assertThat(foundFile).isNotNull();
        assertThat(foundFile.getId()).isEqualTo(savedFile.getId());
    }

    @Test
    @DisplayName("ID 목록으로 파일 조회 시 FileResponse 목록을 반환한다")
    void getFiles_withListOfIds_returnsListOfFileResponses() {
        // GIVEN
        FileEntity file1 = createAndSaveTempFileEntity();
        FileEntity tempFile = FileEntity.builder()
                .filePath("temp/some-temp-file2.png")
                .originalFileName("temp.png")
                .contentType("image/png")
                .build(); // 초기 상태는 TEMP
        FileEntity file2 = fileRepository.save(tempFile);

        List<Long> ids = List.of(file1.getId(), file2.getId());

        // WHEN
        List<FileResponse> responses = fileService.getFiles(ids);

        // THEN
        assertThat(responses).hasSize(2);
        assertThat(responses.stream().map(FileResponse::id)).containsExactlyInAnyOrder(file1.getId(), file2.getId());
    }

    @Test
    @DisplayName("빈 ID 목록으로 파일 조회 시 빈 리스트를 반환한다")
    void getFiles_withEmptyList_returnsEmptyList() {
        // WHEN
        List<FileResponse> responses = fileService.getFiles(List.of());

        // THEN
        assertThat(responses).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("로컬 저장 전략일 때 올바른 URL 형식의 FileResponse를 반환한다")
    void getFileResponse_withLocalStrategy_returnsCorrectUrl() {
        // GIVEN: 로컬 전략을 사용하도록 서비스 필드 값을 강제로 변경하여 테스트
        org.springframework.test.util.ReflectionTestUtils.setField(fileService, "storageStrategyType", "local");
        FileEntity file = createAndSaveTempFileEntity();

        // WHEN
        FileResponse response = fileService.getFileResponse(file);

        // THEN
        assertThat(response.url()).isEqualTo("/files/" + file.getFilePath());
        assertThat(response.id()).isEqualTo(file.getId());
    }

    // S3Presigner는 MockBean으로 등록되어 있지만, 이 테스트에서는 사용되지 않아 별도의 when() 설정은 불필요.
    @Test
    @DisplayName("S3 저장 전략일 때 올바른 URL 형식의 FileResponse를 반환한다")
    void getFileResponse_withS3Strategy_returnsCorrectUrl() {
        // GIVEN: S3 전략을 사용하도록 서비스 필드 값을 강제로 변경하여 테스트
        org.springframework.test.util.ReflectionTestUtils.setField(fileService, "storageStrategyType", "s3");
        org.springframework.test.util.ReflectionTestUtils.setField(fileService, "publicUrl", "https://my-cdn.com");
        org.springframework.test.util.ReflectionTestUtils.setField(fileService, "bucket", "my-bucket");
        FileEntity file = createAndSaveTempFileEntity();

        // WHEN
        FileResponse response = fileService.getFileResponse(file);

        // THEN
        assertThat(response.url()).isEqualTo("https://my-cdn.com/my-bucket/" + file.getFilePath());
    }

    @Test
    @DisplayName("getFileResponse에 null이 전달될 경우 null을 반환한다")
    void getFileResponse_withNullEntity_returnsNull() {
        // WHEN
        FileResponse response1 = fileService.getFileResponse((FileEntity) null);
        FileResponse response2 = fileService.getFileResponse((Long) null);

        // THEN
        assertThat(response1).isNull();
        assertThat(response2).isNull();
    }
}