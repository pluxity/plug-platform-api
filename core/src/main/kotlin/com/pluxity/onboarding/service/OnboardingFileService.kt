package com.pluxity.onboarding.service

import com.pluxity.file.entity.FileEntity
import com.pluxity.file.repository.FileRepository
import com.pluxity.file.strategy.storage.FileProcessingContext
import com.pluxity.file.strategy.storage.StorageStrategy
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.FileUtils
import com.pluxity.onboarding.dto.OnboardingFileResponse
import org.springframework.beans.factory.annotation.Autowired
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
            // 운영체제의 임시폴더에 파일 저장
            val createTempFile = FileUtils.createTempFile(file.originalFilename!!)
            file.transferTo(createTempFile)

            // 스토리지에 저장 (Local/S3)
            val filePath = storage.save(
                FileProcessingContext(
                    contentType = FileUtils.getContentType(file),
                    tempPath = createTempFile,
                    originalFileName = file.originalFilename!!
                )
            )

            // file 엔티티 생성 및 저장 (status: TEMP)
            val savedFile = fileRepository.save(
                FileEntity(
                    filePath = filePath,
                    originalFileName = file.originalFilename!!,
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
}