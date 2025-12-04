package com.pluxity.onboarding.service

import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityRepository
import com.pluxity.onboarding.dto.OnboardingFacilityRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class OnboardingFacilityService @Autowired constructor(
    private val facilityRepository: FacilityRepository,
    private val fileService: OnboardingFileService
){
    private val prefix = "onboarding/"

    fun save(
        facility: Facility,
        request: OnboardingFacilityRequest
    ): Facility {
        // facility 저장
        facility.updateThumbnailFileId(request.thumbnailId)

        val savedFacility = facilityRepository.save(facility)

        val filePath = "$prefix${savedFacility.id}/"
        // 썸네일 file 저장
        request.thumbnailId?.let { thumbnailId ->
            fileService.persistFile(thumbnailId, filePath)
            facility.updateThumbnailFileId(thumbnailId)
        }

        return savedFacility
    }


}