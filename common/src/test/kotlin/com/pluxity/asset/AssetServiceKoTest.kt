package com.pluxity.asset

import asset.dummyAsset
import asset.dummyAssetCategory
import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.asset.repository.AssetRepository
import com.pluxity.asset.service.AssetCategoryService
import com.pluxity.asset.service.AssetService
import com.pluxity.feature.service.FeatureService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.entity.FileEntity
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class AssetServiceKoTest :
    BehaviorSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        val assetRepository = mockk<AssetRepository>()
        val assetCategoryRepository = mockk<AssetCategoryRepository>()
        val fileService = mockk<FileService>(relaxed = true)
        val assetCategoryService = mockk<AssetCategoryService>()
        val featureService = mockk<FeatureService>()

        val service =
            AssetService(
                assetRepository,
                assetCategoryRepository,
                fileService,
                assetCategoryService,
                featureService,
            )

        Given("에셋 ID로 단건 조회 요청할 때") {
            When("ID가 유효하지 않다면") {
                val invalidID = 999L

                every { assetRepository.findByIdOrNull(invalidID) } returns null
                val exception =
                    shouldThrow<CustomException> {
                        service.findById(invalidID)
                    }
                Then("NOT_FOUND_ASSET 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_ASSET.getMessage().format(invalidID)
                    verify(exactly = 1) { assetRepository.findByIdOrNull(invalidID) }
                }
            }

            When("유효한 ID라면") {
                val validID = 1L

                val assetCategory = dummyAssetCategory(id = 1L, iconFileId = 2L)
                val asset =
                    dummyAsset(
                        id = validID,
                        category = assetCategory,
                        name = "test-asset",
                        code = "test-code",
                        fileId = null,
                        thumbnailFileId = null,
                    )

                every { assetRepository.findByIdOrNull(validID) } returns asset

                val result = service.findById(validID)

                Then("Asset 객체를 반환한다.") {
                    result.id shouldBe validID
                    result.category!!.id shouldBe 1L
                    result.name shouldBe "test-asset"
                }
            }
        }

        Given("에셋 목록 조회를 요청할 때") {
            val assetCategory = dummyAssetCategory(id = 1L, iconFileId = 2L)
            val asset1 = dummyAsset(1L, "asset1", "code1", assetCategory, 10L, 20L)
            val asset2 = dummyAsset(2L, "asset2", "code2", assetCategory, 11L, null)
            val assets = listOf(asset1, asset2)

            every { assetRepository.findAll(any<Sort>()) } returns assets

            every { fileService.getFiles(any()) } returns
                listOf(
                    FileResponse(id = 10L, originalFileName = "test-file-1"),
                    FileResponse(id = 11L, originalFileName = "test-file-2"),
                    FileResponse(id = 20L, originalFileName = "test-thumbnail-1"),
                )

            When("정상적으로 호출이 되면") {
                val result = service.getAssets()

                Then("에셋 정보와 파일 정보가 매핑된 Response 리스트를 반환한다.") {
                    result.size shouldBe 2
                    result[0].file?.id shouldBe 10L
                    result[0].thumbnailFile?.id shouldBe 20L
                    result[1].file?.id shouldBe 11L
                    result[1].thumbnailFile shouldBe null

                    verify(exactly = 1) { assetRepository.findAll(any<Sort>()) }
                    verify(exactly = 1) { fileService.getFiles(any()) }
                }
            }
        }

        Given("특정 에셋 카테고리에 속한 에셋 조회 요청할 때") {
            When("카테고리 ID가 유효하고") {
                var validCategoryId = 1L
                var category = dummyAssetCategory(id = validCategoryId, iconFileId = 2L)

                And("해당 카테고리에 속한 에셋이 없다면") {
                    every { assetCategoryService.findById(validCategoryId) } returns category
                    every { assetRepository.findByCategory(category) } returns emptyList()

                    var result = service.getAssetsByCategory(validCategoryId)

                    Then("빈 리스트를 반환한다.") {
                        result shouldBe emptyList()
                    }
                }

                And("에셋이 있고 모든 파일이 존재한다면") {
                    val asset = dummyAsset(id = 1L, fileId = 10L, thumbnailFileId = 11L)

                    every { assetCategoryService.findById(validCategoryId) } returns category
                    every { assetRepository.findByCategory(category) } returns listOf(asset)
                    every { fileService.getFiles(listOf(10L, 11L)) } returns
                        listOf(
                            FileResponse(id = 10L, originalFileName = "test-file-1"),
                            FileResponse(id = 11L, originalFileName = "test-file-2"),
                        )
                    val result = service.getAssetsByCategory(validCategoryId)

                    Then("완전한 AssetResponse 반환") {
                        result.size shouldBe 1
                        result[0].file.shouldNotBeNull()
                        result[0].thumbnailFile.shouldNotBeNull()
                    }
                }
            }
        }

        Given("에셋 생성 요청 할 때") {
            When("에셋 name이 중복이라면") {
                var request = AssetCreateRequest("test-asset", "test-code", null, null, null)
                every { assetRepository.findByName(request.name) } returns dummyAsset(name = "test-asset")

                var exception =
                    shouldThrow<CustomException> {
                        service.createAsset(request)
                    }

                Then("DUPLICATE_ASSET_NAME 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.DUPLICATE_ASSET_NAME.getMessage().format(request.name)
                    verify(exactly = 1) { assetRepository.findByName(request.name) }
                    verify(exactly = 0) { assetRepository.save(any()) }
                }
            }
            When("에셋 코드가 중복이라면") {
                var request = AssetCreateRequest("test-asset", "test-code", null, null, null)
                every { assetRepository.findByName(request.name) } returns null
                every { assetRepository.findByCode(request.code) } returns dummyAsset(name = "test-code")

                var exception =
                    shouldThrow<CustomException> {
                        service.createAsset(request)
                    }
                Then("DUPLICATE_ASSET_CODE 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.DUPLICATE_ASSET_CODE.getMessage().format(request.code)
                    verify(exactly = 1) { assetRepository.findByName(request.name) }
                    verify(exactly = 1) { assetRepository.findByCode(request.code) }
                    verify(exactly = 0) { assetRepository.save(any()) }
                }
            }
            When("카테고리 ID가 유효하지 않다면") {
                var invalidCategoryId = 999L
                var request = AssetCreateRequest("test-asset", "test-code", null, null, invalidCategoryId)
                every { assetRepository.findByName(request.name) } returns null
                every { assetRepository.findByCode(request.code) } returns null
                every { assetCategoryRepository.findByIdOrNull(invalidCategoryId) } returns null

                var exception =
                    shouldThrow<CustomException> {
                        service.createAsset(request)
                    }
                Then("NOT_FOUND_ASSET_CATEGORY 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_ASSET_CATEGORY.getMessage().format(invalidCategoryId)
                    verify(exactly = 1) { assetRepository.findByName(request.name) }
                    verify(exactly = 1) { assetRepository.findByCode(request.code) }
                    verify(exactly = 1) { assetCategoryRepository.findByIdOrNull(invalidCategoryId) }
                    verify(exactly = 0) { assetRepository.save(any()) }
                }
            }

            When("정상적으로 에셋 생성이 성공하면") {
                var validCategoryId = 999L
                var validFileId = 999L

                var request = AssetCreateRequest("test-asset", "test-code", validFileId, null, validCategoryId)

                var category = dummyAssetCategory(id = validCategoryId)
                var fileEntity = FileEntity(originalFileName = "test-file", filePath = "test-path", contentType = "image/png")

                every { assetRepository.findByName(request.name) } returns null
                every { assetRepository.findByCode(request.code) } returns null
                every { assetCategoryRepository.findByIdOrNull(validCategoryId) } returns category
                every { fileService.finalizeUpload(validFileId, any()) } returns fileEntity
                every { assetRepository.save(any()) } returns
                    dummyAsset(id = 1L, name = request.name, code = request.code, category = category, fileId = validFileId)

                var result = service.createAsset(request)

                Then("생성된 에셋 ID를 반환한다.") {

                    result shouldBe 1L
                    verify(exactly = 1) { assetRepository.save(any()) }
                }
            }
        }

        Given("에셋 수정 요청 할 때") {
            When("수정 요청한 이름을 가진 에셋이 이미 존재한다면") {
                var request = AssetUpdateRequest("test-asset", "test-code", null, null, null)
                var assetId = 1L

                every { assetRepository.findByNameAndIdNot(request.name, assetId) } returns dummyAsset()
                var exception =
                    shouldThrow<CustomException> {
                        service.updateAsset(assetId, request)
                    }

                Then("DUPLICATE_ASSET_NAME 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.DUPLICATE_ASSET_NAME.getMessage().format(request.name)
                }
            }

            When("수정 요청한 코드를 가진 에셋이 이미 존재한다면") {
                var request = AssetUpdateRequest("test-asset", "test-code", null, null, null)
                var assetId = 1L

                every { assetRepository.findByNameAndIdNot(request.name, assetId) } returns null
                every { assetRepository.findByCodeAndIdNot(request.code, assetId) } returns dummyAsset()
                var exception =
                    shouldThrow<CustomException> {
                        service.updateAsset(assetId, request)
                    }

                Then("DUPLICATE_ASSET_CODE 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.DUPLICATE_ASSET_CODE.getMessage().format(request.code)
                }
            }

            When("수정 요청한 에셋이 존재하지 않다면") {
                var invalidAssetId = 999L
                var request = AssetUpdateRequest("test-asset", "test-code", null, null, null)

                every { assetRepository.findByNameAndIdNot(request.name, invalidAssetId) } returns null
                every { assetRepository.findByCodeAndIdNot(request.code, invalidAssetId) } returns null
                every { assetRepository.findByIdOrNull(invalidAssetId) } returns null
                var exception =
                    shouldThrow<CustomException> {
                        service.updateAsset(invalidAssetId, request)
                    }
                Then("NOT_FOUND_ASSET 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_ASSET.getMessage().format(invalidAssetId)
                }
            }

            When("카테고리 ID가 유효하지 않다면") {
                var invalidCategoryId = 999L
                var request = AssetUpdateRequest("test-asset", "test-code", null, null, invalidCategoryId)
                var assetId = 1L
                every { assetRepository.findByNameAndIdNot(request.name, assetId) } returns null
                every { assetRepository.findByCodeAndIdNot(request.code, assetId) } returns null
                every { assetRepository.findByIdOrNull(assetId) } returns dummyAsset(id = assetId)
                every { assetCategoryRepository.findByIdOrNull(invalidCategoryId) } returns null

                var exception = shouldThrow<CustomException> { service.updateAsset(assetId, request) }

                Then("NOT_FOUND_ASSET_CATEGORY 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_ASSET_CATEGORY.getMessage().format(invalidCategoryId)
                }
            }

            When("정상적으로 에셋 수정이 성공하면") {
                var asset = dummyAsset(id = 1L, name = "before-asset", code = "before-code")
                var validCategoryId = 999L
                var request = AssetUpdateRequest("after-asset", "after-code", null, null, validCategoryId)
                var assetId = 1L
                var category = dummyAssetCategory(id = validCategoryId)
                every { assetRepository.findByNameAndIdNot(request.name, assetId) } returns null
                every { assetRepository.findByCodeAndIdNot(request.code, assetId) } returns null
                every { assetCategoryRepository.findByIdOrNull(validCategoryId) } returns category
                every { assetRepository.findByIdOrNull(assetId) } returns asset

                service.updateAsset(assetId, request)

                Then("에셋 정보가 정상적으로 업데이트된다.") {
                    asset.name shouldBe "after-asset"
                    asset.code shouldBe "after-code"
                    asset.category shouldBe category
                }
            }
        }

        Given("에셋 삭제 요청 할 때") {
            When("에셋 ID가 유효하지 않다면") {
                var invalidAssetId = 999L
                every { assetRepository.findByIdOrNull(invalidAssetId) } returns null

                var exception = shouldThrow<CustomException> { service.deleteAsset(invalidAssetId) }
                Then("NOT_FOUND_ASSET 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_ASSET.getMessage().format(invalidAssetId)
                }
            }

            When("정상적으로 에셋 삭제가 성공하면") {
                var validAssetId = 1L
                var asset = dummyAsset(id = validAssetId, category = dummyAssetCategory(id = 1L))

                every { assetRepository.findByIdOrNull(validAssetId) } returns asset
                every { featureService.findFeatureIdsByAssetId(validAssetId) } returns emptyList()
                every { featureService.deleteFeature(any()) } just runs
                every { assetRepository.delete(asset) } just runs

                service.deleteAsset(validAssetId)

                Then("연관관계 정리 후 삭제 완료") {
                    asset.category shouldBe null
                    verify(exactly = 1) { assetRepository.delete(asset) }
                }
            }
        }

        Given("에셋 카테고리 변경 요청 할 때") {
            When("에셋 ID가 유효하지 않다면") {
                var invalidAssetId = 999L
                every { assetRepository.findByIdOrNull(invalidAssetId) } returns null

                var exception = shouldThrow<CustomException> { service.assignCategory(invalidAssetId, 1L) }
                Then("NOT_FOUND_ASSET 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_ASSET.getMessage().format(invalidAssetId)
                }
            }
            When("정상적으로 카테고리 변경이 성공하면") {
                var validAssetId = 1L
                var categoryId = 2L
                var category = dummyAssetCategory(id = 1L, categoryName = "before-category")
                var assignCategory = dummyAssetCategory(id = 2L, categoryName = "after-category")
                var asset = dummyAsset(id = validAssetId, category = category)

                every { assetRepository.findByIdOrNull(validAssetId) } returns asset
                every { assetCategoryService.findById(categoryId) } returns assignCategory

                service.assignCategory(validAssetId, 2L)
                Then("에셋의 카테고리가 지정한 카테고리로 변경되어야 한다") {
                    asset.category?.id shouldBe 2L
                    asset.category?.name shouldBe "after-category"
                    verify(exactly = 1) { assetRepository.findByIdOrNull(validAssetId) }
                    verify(exactly = 1) { assetCategoryService.findById(categoryId) }
                }
            }
        }
    })
