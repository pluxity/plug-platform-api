package com.pluxity.cctv

import cctv.dummyCctv
import com.pluxity.cctv.dto.CctvCreateRequest
import com.pluxity.cctv.dto.CctvUpdateRequest
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.cctv.repository.DeviceCctvRepository
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import file.dummyFileResponse
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID

class CctvServiceKoTest : BehaviorSpec({

    val cctvRepository: CctvRepository = mockk()
    val deviceCategoryService: DeviceCategoryService = mockk()
    val fileService: FileService = mockk()
    val deviceCctvRepository: DeviceCctvRepository = mockk()

    val cctvService =
        CctvService(
            cctvRepository,
            deviceCategoryService,
            fileService,
            deviceCctvRepository,
        )
    Given("CCTV 생성을 진행할 때") {
        When("유효한 요청으로 CCTV 생성 요청") {
            val id = UUID.randomUUID().toString()
            val createRequest = CctvCreateRequest(id, "cctv-name", "url", null)

            every {
                cctvRepository.save(any())
            } returns dummyCctv(id = id)
            Then("성공") {
                val saveId = cctvService.create(createRequest)
                saveId shouldBe createRequest.id
            }
        }
    }

    Given("CCTV 목록 조회를 진행할 때") {
        When("정상 요청이 오면") {
            every {
                cctvRepository.findAll(any<Sort>())
            } returns mutableListOf(dummyCctv())

            every {
                fileService.getFiles(any())
            } returns mutableListOf(dummyFileResponse())

            Then("정상 조회") {
                cctvService.findAll().size shouldBe 1
            }
        }
    }

    Given("CCTV 상세 조회를 진행할 때") {
        When("유효한 아이디로 조회 요청") {
            val cctv = dummyCctv()
            every {
                cctvRepository.findByIdOrNull(any())
            } returns cctv
            Then("정상 조회") {
                val res = cctvService.findById(cctv.id)
                res.id shouldBe cctv.id
                res.name shouldBe cctv.name
            }
        }

        When("없는 아이디로 조회 요청") {
            every {
                cctvRepository.findByIdOrNull(any())
            } returns null
            Then("NOT_FOUND_CCTV 예외 발생") {
                val searchId = UUID.randomUUID().toString()
                shouldThrowExactly<CustomException> {
                    cctvService.findById(searchId)
                }.message shouldBe ErrorCode.NOT_FOUND_CCTV.message.format(searchId)
            }
        }
    }

    Given("CCTV 수정을 진행할 때") {
        When("정상 수정 요청") {
            val cctv = dummyCctv()
            every {
                cctvRepository.findByIdOrNull(any())
            } returns cctv
            Then("정상 수정") {
                val updateName = "updated Cctv"
                cctvService.update(cctv.id, CctvUpdateRequest(updateName, "", null))
                cctv.name shouldBe updateName
            }
        }
    }

    Given("CCTV 삭제를 진행할 때") {
        When("정상 삭제 요청") {
            val cctv = dummyCctv()
            val slot = slot<String>()
            every {
                cctvRepository.findByIdOrNull(any())
            } returns cctv
            every {
                deviceCctvRepository.deleteByCctvIdIn(any())
            } just runs
            every {
                cctvRepository.deleteById(capture(slot))
            } just runs
            Then("정상 삭제") {
                cctvService.delete(cctv.id)
                verify(exactly = 1) { cctvRepository.deleteById(any()) }
                slot.captured shouldBe cctv.id
            }
        }
    }
})
