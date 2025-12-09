package com.pluxity.onboarding.service

import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityRepository
import com.pluxity.onboarding.dto.OnboardingFacilityRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OnboardingFacilityService
    @Autowired
    constructor(
        private val facilityRepository: FacilityRepository,
        private val fileService: OnboardingFileService,
    ) {
        private val prefix = "onboarding/"

        @Transactional
        fun save(
            facility: Facility,
            request: OnboardingFacilityRequest,
        ): Facility {
            // facility를 저장
            val savedFacility = facilityRepository.save(facility)

            request.thumbnailId?.let { thumbnailId ->
                val filePath = "$prefix${savedFacility.id!!}/"

                fileService.persistFile(thumbnailId, filePath)

                savedFacility.updateThumbnailFileId(thumbnailId)
            }
            return savedFacility
        }
    }
