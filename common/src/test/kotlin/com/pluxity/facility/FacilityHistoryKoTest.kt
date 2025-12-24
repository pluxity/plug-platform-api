package com.pluxity.facility

import com.pluxity.facility.history.FacilityHistory
import com.pluxity.facility.history.FacilityHistoryRepository
import com.pluxity.facility.history.FacilityHistoryService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class FacilityHistoryKoTest :
    BehaviorSpec({

        val facilityHistoryRepository = mockk<FacilityHistoryRepository>()
        val fileService = mockk<FileService>()
        val facilityHistoryService = FacilityHistoryService(facilityHistoryRepository, fileService)

        Given("ID로 조회 요청이 주어질때") {
            val facilityId = 1L
            When("해당하는 history를 찾을 수 없다면") {
                every { facilityHistoryRepository.findByFacilityIdOrderByCreatedAtDesc(facilityId) } returns emptyList()
                val result = facilityHistoryService.findByFacilityId(facilityId)
                Then("빈 리스트를 반환한다.") {
                    result shouldBe emptyList()
                    verify(exactly = 1) {
                        facilityHistoryRepository.findByFacilityIdOrderByCreatedAtDesc(facilityId)
                    }
                }
            }
            When("해당하는 history가 있다면") {
                val facilityMock1 =
                    mockk<FacilityHistory>(relaxed = true) {
                        every { fileId } returns 1L
                    }
                val facilityMock2 =
                    mockk<FacilityHistory>(relaxed = true) {
                        every { fileId } returns 2L
                    }

                val fileResponse1 =
                    mockk<FileResponse>(relaxed = true) {
                        every { id } returns 1L
                    }
                val fileResponse2 =
                    mockk<FileResponse>(relaxed = true) {
                        every { id } returns 2L
                    }

                every {
                    facilityHistoryRepository.findByFacilityIdOrderByCreatedAtDesc(facilityId)
                } returns listOf(facilityMock1, facilityMock2)

                every {
                    fileService.getFiles(listOf(1L, 2L))
                } returns listOf(fileResponse1, fileResponse2)

                val result = facilityHistoryService.findByFacilityId(facilityId)

                Then("FacilityHistoryResponse 리스트를 반환한다.") {
                    result.size shouldBe 2
                    result[0].file shouldBe fileResponse1
                    result[1].file shouldBe fileResponse2
                }
            }
            When("해당하는 history가 있지만 파일을 찾을 수 없다면") {
                val facilityMock1 =
                    mockk<FacilityHistory>(relaxed = true) {
                        every { fileId } returns 1L
                    }

                every {
                    facilityHistoryRepository.findByFacilityIdOrderByCreatedAtDesc(facilityId)
                } returns listOf(facilityMock1)

                every {
                    fileService.getFiles(any<List<Long>>())
                } returns emptyList()

                Then("빈 리스트를 반환한다") {
                    val result = facilityHistoryService.findByFacilityId(facilityId)
                    result shouldBe emptyList()
                    verify(exactly = 1) {
                        facilityHistoryRepository.findByFacilityIdOrderByCreatedAtDesc(facilityId)
                    }
                    verify(exactly = 1) {
                        fileService.getFiles(any<List<Long>>())
                    }
                }
            }
        }
    })
