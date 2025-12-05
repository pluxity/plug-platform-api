package com.pluxity.onboarding.service

import com.pluxity.file.constant.FileStatus
import com.pluxity.file.entity.FileEntity
import com.pluxity.file.repository.FileRepository
import com.pluxity.file.strategy.storage.FilePersistenceContext
import com.pluxity.file.strategy.storage.FileProcessingContext
import com.pluxity.file.strategy.storage.StorageStrategy
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.FileUtils
import com.pluxity.onboarding.dto.OnboardingFileResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class OnboardingFileService @Autowired constructor(
    private val storage: StorageStrategy,
    private val fileRepository: FileRepository
){
    /**
     * 파일을 임시 저장소에 업로드
     *
     * 순서
     * 1. MultipartFile을 OS 임시 디렉토리에 저장
     * 2. 설정된 StorageStrategy(Local/S3)의 temp/ 경로에 업로드
     * 3. FileEntity 생성 및 DB 저장 (status: TEMP)
     * 4. OS 임시 파일 삭제
     *
     * 업로드된 파일은 TEMP 상태로 저장되고, 실제 엔티티(Asset, Building 등)가
     * 생성될 때 finalizeUpload()를 통해 영구 경로로 이동되고 COMPLETE 상태로 변경됨
     *
     * @param file 업로드할 파일 (MultipartFile)
     * @return OnboardingFileResponse 업로드된 파일 정보 (ID, 경로)
     * @throws CustomException 파일 업로드 실패 시 (FAILED_TO_UPLOAD_FILE)
     *
     */
    @Transactional
    fun uploadFile(file: MultipartFile): OnboardingFileResponse {
        try {
            val originalFileName = file.originalFilename
                ?: throw CustomException(ErrorCode.INVALID_FILE_STATUS, "파일 원본 이름이 없습니다.")

            // 운영체제의 임시폴더에 파일 저장
            val createTempFile = FileUtils.createTempFile(originalFileName)
            file.transferTo(createTempFile)

            // 스토리지에 저장 (Local/S3)
            val filePath = storage.save(
                FileProcessingContext(
                    contentType = FileUtils.getContentType(file),
                    tempPath = createTempFile,
                    originalFileName = originalFileName
                )
            )

            // file 엔티티 생성 및 저장 (status: TEMP)
            val savedFile = fileRepository.save(
                FileEntity(
                    filePath = filePath,
                    originalFileName = originalFileName,
                    contentType = FileUtils.getContentType(file)
                )
            )

            // 임시폴더 파일 삭제
            FileUtils.deleteDirectoryRecursively(createTempFile)

            return OnboardingFileResponse(
                id = savedFile.id!!,
                url = savedFile.filePath
            )
        } catch (e: Exception) {
            throw CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE, e.message)
        }
    }

    /**
     * 임시 파일을 영구 저장소로 이동하고 COMPLETE 상태로 변경
     *
     * uploadFile()로 업로드된 TEMP 상태의 파일을
     * 실제 엔티티(Asset, Building 등)의 최종 경로로 이동시킴
     *
     * 과정:
     * 1. fileId로 FileEntity 조회
     * 2. TEMP 상태 검증
     * 3. StorageStrategy를 통해 파일 이동
     *    - Local: temp/file.jpg → assets/123/file.jpg
     *    - S3: bucket/temp/file.jpg → bucket/assets/123/file.jpg
     * 4. FileEntity 상태를 COMPLETE로 변경 및 경로 업데이트
     *
     * @param fileId 영구 저장할 파일 ID
     * @param newPath 새로운 파일 경로 (예: "assets/123/", "buildings/456/")
     * @throws CustomException NOT_FOUND_FILE - 파일을 찾을 수 없음
     * @throws CustomException INVALID_FILE_STATUS - TEMP 상태가 아님
     * @throws CustomException FAILED_TO_UPLOAD_FILE - 파일 이동 실패
     */
    @Transactional
    fun persistFile(fileId: Long, newPath: String) {
        // 영구저장할 파일 조회
        val file = fileRepository.findByIdOrNull(fileId)
            ?: throw CustomException(ErrorCode.NOT_FOUND_FILE, fileId)

        // TEMP 상태 검증
        require(file.fileStatus == FileStatus.TEMP) {
            throw CustomException(ErrorCode.INVALID_FILE_STATUS, "임시 파일이 아닌 경우에는 영구 저장할 수 없습니다")
        }

        // 임시 저장된 파일을 newPath로 이동
        val path = storage.persist(
            FilePersistenceContext(
                filePath = file.filePath,           // 기존 파일 경로 (temp/)
                newPath = newPath,                   // 새로운 경로 (assets/123/)
                contentType = file.contentType,
                originalFileName = file.originalFileName
            )
        )

        // FileEntity 상태를 COMPLETE로 변경하고 새 경로 업데이트
        file.makeComplete(path)
    }

    /**
     * TEMP 상태의 임시 파일을 삭제
     *
     * TEMP 상태의 파일만 삭제 가능하고, COMPLETE 상태의 파일은 삭제할 수 없음
     *
     * 과정:
     * 1. fileId로 FileEntity 조회
     * 2. TEMP 상태 검증
     * 3. StorageStrategy를 통해 실제 파일 삭제
     * 4. DB에서 FileEntity 삭제
     *
     * 사용 예)
     * - 사용자가 파일 업로드 후 취소한 경우
     * - 파일 업로드 후 엔티티 생성 실패한 경우
     *
     * @param fileId 삭제할 파일 ID
     * @throws CustomException NOT_FOUND_FILE - 파일을 찾을 수 없음
     * @throws CustomException INVALID_FILE_STATUS - TEMP 상태가 아닌 파일은 삭제 불가
     * @throws CustomException FAILED_TO_DELETE_FILE - 파일 삭제 실패
     */
    @Transactional
    fun deleteTempFile(fileId: Long) {
        // id로 파일 찾기
        val file = (fileRepository.findByIdOrNull(fileId)
            ?: throw CustomException(ErrorCode.NOT_FOUND_FILE, fileId))

        // TEMP 상태 검증
        require(file.fileStatus == FileStatus.TEMP) {
            throw CustomException(ErrorCode.INVALID_FILE_STATUS, "임시 파일이 아닌 경우에는 삭제할 수 없습니다")
        }

        // 스토리지에서 실제 파일 삭제 (Local 또는 S3)
        storage.delete(file.filePath)

        // DB에서 FileEntity 삭제
        fileRepository.delete(file)
    }
}