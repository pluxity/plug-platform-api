package com.pluxity.file

import com.pluxity.config.MockBeansConfig
import com.pluxity.file.constant.FileStatus
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.entity.FileEntity
import com.pluxity.file.repository.FileRepository
import com.pluxity.file.service.FileService
import com.pluxity.file.strategy.storage.StorageStrategy
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import java.nio.file.Path
import java.util.UUID

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class FileServiceTest {
    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var fileRepository: FileRepository

    // 실제 파일 시스템/S3 대신 가짜 객체로 대체하여 테스트
    @MockitoBean
    private lateinit var storageStrategy: StorageStrategy

    private lateinit var testFile: MockMultipartFile
    private lateinit var tempFilePath: String

    @BeforeEach
    @Throws(Exception::class)
    fun setUp() {
        // GIVEN: 모든 테스트에서 사용할 기본 MockMultipartFile 생성
        testFile =
            MockMultipartFile("file", "test.png", "image/png", "test-file-content".toByteArray())

        // GIVEN: storageStrategy.save가 반환할 가상 경로 설정
        tempFilePath = "temp/${UUID.randomUUID()}.png"
        Mockito
            .`when`(
                storageStrategy.save(any()),
            ).thenReturn(tempFilePath)
    }

    // 테스트에서 임시 상태의 파일을 쉽게 만들기 위한 헬퍼 메서드
    private fun createAndSaveTempFileEntity(): FileEntity {
        val tempFile =
            FileEntity(
                filePath = "temp/some-temp-file.png",
                originalFileName = "temp.png",
                contentType = "image/png",
            )
        return fileRepository.save(tempFile)
    }

    @Test
    @DisplayName("파일 업로드 시작 시 임시 파일 엔티티를 생성하고 ID를 반환한다")
    @Throws(Exception::class)
    fun initiateUpload_withValidFile_createsTempFileEntityAndReturnsId() {
        // WHEN
        val fileId = fileService.initiateUpload(testFile)

        // THEN
        Assertions.assertThat(fileId).isNotNull()

        // 1. StorageStrategy의 save 메소드가 정확히 1번 호출되었는지 검증
        Mockito
            .verify(storageStrategy, Mockito.times(1))
            ?.save(any())

        // 2. DB에 파일 정보가 'TEMP' 상태로 저장되었는지 검증
        val savedFileOpt = fileRepository.findById(fileId)
        Assertions.assertThat(savedFileOpt).isPresent()
        val savedFile = savedFileOpt.get()

        Assertions.assertThat(savedFile.originalFileName).isEqualTo("test.png")
        Assertions.assertThat(savedFile.contentType).isEqualTo("image/png")
        Assertions.assertThat(savedFile.filePath).isEqualTo(tempFilePath) // Mock으로 설정한 경로
        Assertions.assertThat(savedFile.fileStatus).isEqualTo(FileStatus.TEMP)
    }

    @Test
    @DisplayName("파일 업로드 중 I/O 오류 발생 시 CustomException을 던진다")
    @Throws(IOException::class)
    fun initiateUpload_whenIoErrorOccurs_throwsCustomException() {
        // GIVEN: 파일 전송 시 IOException을 발생시키는 Mock 객체 생성
        val failingFile = Mockito.mock(MockMultipartFile::class.java)
        Mockito.`when`(failingFile.originalFilename).thenReturn("failing.txt")
        Mockito
            .doThrow(IOException("Disk is full"))
            .`when`(failingFile)
            .transferTo(any<Path>())

        // WHEN & THEN
        val exception =
            assertThrows<CustomException> {
                fileService.initiateUpload(failingFile)
            }

        Assertions.assertThat(exception.message).contains(ErrorCode.FAILED_TO_UPLOAD_FILE.getMessage())
    }

    @Test
    @DisplayName("임시 파일을 영구 저장 시 상태와 경로를 업데이트한다")
    @Throws(Exception::class)
    fun finalizeUpload_withTempFile_updatesStatusAndPath() {
        // GIVEN: 임시 상태의 파일 엔티티를 미리 저장
        val tempFile = createAndSaveTempFileEntity()
        val permanentPath = "permanent/" + UUID.randomUUID() + ".png"
        Mockito
            .`when`(
                storageStrategy.persist(
                    any(),
                ),
            ).thenReturn(permanentPath)

        // WHEN
        val finalizedFile = fileService.finalizeUpload(tempFile.id!!, permanentPath)

        // THEN
        // 1. StorageStrategy의 persist 메소드가 1번 호출되었는지 검증
        try {
            Mockito
                .verify(storageStrategy, Mockito.times(1))
                .persist(any())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        // 2. 반환된 엔티티와 DB의 상태가 'COMPLETE'로 변경되었는지 검증
        Assertions.assertThat(finalizedFile.fileStatus).isEqualTo(FileStatus.COMPLETE)
        Assertions.assertThat(finalizedFile.filePath).isEqualTo(permanentPath)

        val updatedFile = fileRepository.findById(tempFile.id!!).orElseThrow()
        Assertions.assertThat(updatedFile.fileStatus).isEqualTo(FileStatus.COMPLETE)
        Assertions.assertThat(updatedFile.filePath).isEqualTo(permanentPath)
    }

    @Test
    @DisplayName("영구 저장 시 파일 ID가 존재하지 않으면 예외를 던진다")
    fun finalizeUpload_withNonExistentId_throwsCustomException() {
        // GIVEN
        val nonExistentId = 9999L

        // WHEN & THEN
        val exception =
            assertThrows<CustomException> {
                fileService.finalizeUpload(nonExistentId, "some/path")
            }

        Assertions.assertThat(exception.errorCode.getMessage()).isEqualTo(ErrorCode.INVALID_FILE_STATUS.getMessage())

        Assertions.assertThat(exception.errorCode.getMessage()).isEqualTo(ErrorCode.INVALID_FILE_STATUS.getMessage())
    }

    @Test
    @DisplayName("임시 상태가 아닌 파일을 영구 저장 시도 시 예외를 던진다")
    fun finalizeUpload_withNonTempFile_throwsCustomException() {
        // GIVEN: 'COMPLETE' 상태의 파일을 생성
        val completeFile = createAndSaveTempFileEntity()
        completeFile.makeComplete("already/persisted.png")
        fileRepository.save(completeFile)

        // WHEN & THEN
        val exception =
            assertThrows<CustomException> {
                fileService.finalizeUpload(completeFile.id!!, "new/path")
            }

        Assertions.assertThat(exception.message).contains(ErrorCode.INVALID_FILE_STATUS.getMessage())
    }

    @Test
    @DisplayName("존재하는 파일 ID로 조회 시 FileEntity를 반환한다")
    fun getFile_withExistingId_returnsFileEntity() {
        // GIVEN
        val savedFile = createAndSaveTempFileEntity()

        // WHEN
        val foundFile = fileService.getFile(savedFile.id!!)

        // THEN
        Assertions.assertThat(foundFile).isNotNull()
        Assertions.assertThat(foundFile.id).isEqualTo(savedFile.id)
    }

    @Test
    @DisplayName("ID 목록으로 파일 조회 시 FileResponse 목록을 반환한다")
    fun getFiles_withListOfIds_returnsListOfFileResponses() {
        // GIVEN
        val file1 = createAndSaveTempFileEntity()
        val tempFile =
            FileEntity(
                filePath = "temp/some-temp-file2.png",
                originalFileName = "temp.png",
                contentType = "image/png",
            )
        val file2 = fileRepository.save(tempFile)

        val ids = listOf(file1.id!!, file2.id!!)

        // WHEN
        val responses = fileService.getFiles(ids)

        // THEN
        Assertions.assertThat(responses).hasSize(2)
        Assertions
            .assertThat(responses.stream().map(FileResponse::id))
            .containsExactlyInAnyOrder(file1.id, file2.id)
    }

    @Test
    @DisplayName("빈 ID 목록으로 파일 조회 시 빈 리스트를 반환한다")
    fun getFiles_withEmptyList_returnsEmptyList() {
        // WHEN
        val responses: List<FileResponse> = fileService.getFiles(emptyList())

        // THEN
        Assertions.assertThat(responses).isNotNull().isEmpty()
    }

    @Test
    @DisplayName("로컬 저장 전략일 때 올바른 URL 형식의 FileResponse를 반환한다")
    fun getFileResponse_withLocalStrategy_returnsCorrectUrl() {
        // GIVEN: 로컬 전략을 사용하도록 서비스 필드 값을 강제로 변경하여 테스트
        ReflectionTestUtils.setField(
            fileService,
            "storageStrategyType",
            "local",
        )
        val file = createAndSaveTempFileEntity()

        // WHEN
        val response = fileService.getFileResponse(file)

        // THEN
        Assertions.assertThat(response?.url).isEqualTo("/files/" + file.filePath)
        Assertions.assertThat(response?.id).isEqualTo(file.id)
    }

    // S3Presigner는 MockBean으로 등록되어 있지만, 이 테스트에서는 사용되지 않아 별도의 when() 설정은 불필요.
    @Test
    @DisplayName("S3 저장 전략일 때 올바른 URL 형식의 FileResponse를 반환한다")
    fun getFileResponse_withS3Strategy_returnsCorrectUrl() {
        // GIVEN: S3 전략을 사용하도록 서비스 필드 값을 강제로 변경하여 테스트
        ReflectionTestUtils.setField(
            fileService,
            "storageStrategyType",
            "s3",
        )
        ReflectionTestUtils.setField(
            fileService,
            "publicUrl",
            "https://my-cdn.com",
        )
        ReflectionTestUtils.setField(fileService, "bucket", "my-bucket")
        val file = createAndSaveTempFileEntity()

        // WHEN
        val response = fileService.getFileResponse(file)

        // THEN
        Assertions.assertThat(response?.url).isEqualTo("https://my-cdn.com/my-bucket/" + file.filePath)
    }

    @Test
    @DisplayName("getFileResponse에 null이 전달될 경우 null을 반환한다")
    fun getFileResponse_withNullEntity_returnsNull() {
        // WHEN
        val response1 = fileService.getFileResponse(null as FileEntity?)
        val response2 = fileService.getFileResponse(null as Long?)

        // THEN
        Assertions.assertThat(response1).isNull()
        Assertions.assertThat(response2).isNull()
    }
}
