package com.pluxity.asset

import asset.dummyAssetCategory
import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCategoryUpdateRequest
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.asset.service.AssetCategoryService
import com.pluxity.category.dto.CategoryDepthResponse
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.entity.FileEntity
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class AssetCategoryServiceKoTest :
    BehaviorSpec({

        isolationMode = IsolationMode.InstancePerLeaf

        val assetCategoryRepository = mockk<AssetCategoryRepository>()
        val fileService = mockk<FileService>(relaxed = true)

        val service =
            AssetCategoryService(
                assetCategoryRepository,
                fileService,
                assetCategoryRepository,
            )

        Given("모든 에셋 카테고리 조회 요청을 할 때") {
            When("카테고리가 비어있다면") {
                every { assetCategoryRepository.findAll(any<Sort>()) } returns emptyList()

                val result = service.getAllCategories()

                Then("빈 리스트를 반환한다.") {
                    result shouldBe emptyList()
                    verify(exactly = 1) { assetCategoryRepository.findAll(any<Sort>()) }
                }
            }

            When("루트 카테고리만 존재한다면") {
                val rootCategory1 = dummyAssetCategory(id = 1L, categoryName = "카테고리1", iconFileId = 10L)
                val rootCategory2 = dummyAssetCategory(id = 2L, categoryName = "카테고리2", iconFileId = 20L)
                val categories = listOf(rootCategory1, rootCategory2)

                every { assetCategoryRepository.findAll(any<Sort>()) } returns categories
                every { fileService.getFiles(any()) } returns
                    listOf(
                        FileResponse(id = 10L, originalFileName = "icon1.png"),
                        FileResponse(id = 20L, originalFileName = "icon2.png"),
                    )

                val result = service.getAllCategories()

                Then("루트 카테고리만 반환하고 파일 정보가 매핑된다.") {
                    result.size shouldBe 2
                    verify(exactly = 1) { assetCategoryRepository.findAll(any<Sort>()) }
                    verify(exactly = 1) { fileService.getFiles(any()) }
                }
            }

            When("루트와 자식 카테고리가 모두 존재한다면") {
                val parentCategory = dummyAssetCategory(id = 1L, categoryName = "부모", iconFileId = 10L)
                val childCategory = dummyAssetCategory(id = 2L, categoryName = "자식", iconFileId = 20L)
                childCategory.parent = parentCategory
                val categories = listOf(parentCategory, childCategory)

                every { assetCategoryRepository.findAll(any<Sort>()) } returns categories
                every { fileService.getFiles(any()) } returns
                    listOf(
                        FileResponse(id = 10L, originalFileName = "parent.png"),
                        FileResponse(id = 20L, originalFileName = "child.png"),
                    )

                val result = service.getAllCategories()

                Then("루트 카테고리만 반환한다.") {
                    result.size shouldBe 1
                    verify(exactly = 1) { assetCategoryRepository.findAll(any<Sort>()) }
                }
            }
        }

        Given("특정 부모의 자식 카테고리 조회 요청을 할 때") {
            When("부모 ID가 유효하고 자식 카테고리가 있다면") {
                val parentId = 1L
                val child1 = dummyAssetCategory(id = 2L, categoryName = "자식1", iconFileId = 10L)
                val child2 = dummyAssetCategory(id = 3L, categoryName = "자식2", iconFileId = 20L)

                every { assetCategoryRepository.findByParentId(parentId) } returns listOf(child1, child2)
                every { fileService.getFileResponse(10L) } returns FileResponse(id = 10L, originalFileName = "child1.png")
                every { fileService.getFileResponse(20L) } returns FileResponse(id = 20L, originalFileName = "child2.png")

                val result = service.getChildCategories(parentId)

                Then("자식 카테고리 리스트를 반환하고 파일 정보가 매핑된다.") {
                    result.size shouldBe 2
                    verify(exactly = 1) { assetCategoryRepository.findByParentId(parentId) }
                }
            }

            When("부모 ID가 유효하지만 자식 카테고리가 없다면") {
                val parentId = 1L

                every { assetCategoryRepository.findByParentId(parentId) } returns emptyList()

                val result = service.getChildCategories(parentId)

                Then("빈 리스트를 반환한다.") {
                    result shouldBe emptyList()
                    verify(exactly = 1) { assetCategoryRepository.findByParentId(parentId) }
                }
            }
        }

        Given("에셋 카테고리 생성 요청을 할 때") {
            When("코드가 중복이라면") {
                val request = AssetCategoryCreateRequest(name = "카테고리", code = "DUPLICATE_CODE")

                every { assetCategoryRepository.existsByCode(request.code) } returns true

                val exception =
                    shouldThrow<CustomException> {
                        service.createAssetCategory(request)
                    }

                Then("DUPLICATE_ASSET_CATEGORY_CODE 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.DUPLICATE_ASSET_CATEGORY_CODE.getMessage().format(request.code)
                    verify(exactly = 1) { assetCategoryRepository.existsByCode(request.code) }
                    verify(exactly = 0) { assetCategoryRepository.save(any()) }
                }
            }

            When("부모 카테고리 ID가 유효하지 않다면") {
                val invalidParentId = 999L
                val request = AssetCategoryCreateRequest(name = "카테고리", code = "CODE", parentId = invalidParentId)

                every { assetCategoryRepository.existsByCode(request.code) } returns false
                every { assetCategoryRepository.findByIdOrNull(invalidParentId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        service.createAssetCategory(request)
                    }

                Then("NOT_FOUND_CATEGORY 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_CATEGORY.getMessage().format(invalidParentId)
                    verify(exactly = 1) { assetCategoryRepository.existsByCode(request.code) }
                    verify(exactly = 1) { assetCategoryRepository.findByIdOrNull(invalidParentId) }
                    verify(exactly = 0) { assetCategoryRepository.save(any()) }
                }
            }

            When("정상적으로 카테고리 생성이 성공하면") {
                val request = AssetCategoryCreateRequest(name = "카테고리", code = "CODE")
                val savedCategory = dummyAssetCategory(id = 1L, categoryName = request.name, code = request.code)

                every { assetCategoryRepository.existsByCode(request.code) } returns false
                every { assetCategoryRepository.save(any()) } returns savedCategory

                val result = service.createAssetCategory(request)

                Then("생성된 카테고리 ID를 반환한다.") {
                    result shouldBe 1L
                    verify(exactly = 1) { assetCategoryRepository.existsByCode(request.code) }
                    verify(exactly = 1) { assetCategoryRepository.save(any()) }
                    verify(exactly = 0) { fileService.finalizeUpload(any(), any()) }
                }
            }

            When("부모 카테고리와 함께 생성하면") {
                val parentId = 1L
                val parent = dummyAssetCategory(id = parentId, categoryName = "부모")
                val request = AssetCategoryCreateRequest(name = "자식", code = "CHILD_CODE", parentId = parentId)
                val savedCategory = dummyAssetCategory(id = 2L, categoryName = request.name, code = request.code)
                savedCategory.parent = parent

                every { assetCategoryRepository.existsByCode(request.code) } returns false
                every { assetCategoryRepository.findByIdOrNull(parentId) } returns parent
                every { assetCategoryRepository.save(any()) } returns savedCategory

                val exception = shouldThrow<CustomException> { service.createAssetCategory(request) }

                Then("EXCEED_CATEGORY_DEPTH 예외가 발생한다.(assetCategory의 MAX_DEPTH = 1)") {
                    exception.errorCode shouldBe ErrorCode.EXCEED_CATEGORY_DEPTH
                }
            }

            When("썸네일 파일 ID와 함께 생성하면") {
                val thumbnailFileId = 10L
                val request = AssetCategoryCreateRequest(name = "카테고리", code = "CODE", thumbnailFileId = thumbnailFileId)
                val savedCategory =
                    dummyAssetCategory(
                        id = 1L,
                        categoryName = request.name,
                        code = request.code,
                        iconFileId = thumbnailFileId,
                    )
                val fileEntity = FileEntity(originalFileName = "icon.png", filePath = "path", contentType = "image/png")

                every { assetCategoryRepository.existsByCode(request.code) } returns false
                every { assetCategoryRepository.save(any()) } returns savedCategory
                every { fileService.finalizeUpload(thumbnailFileId, any()) } returns fileEntity

                val result = service.createAssetCategory(request)

                Then("파일이 finalize되고 카테고리 ID를 반환한다.") {
                    result shouldBe 1L
                    verify(exactly = 1) { fileService.finalizeUpload(thumbnailFileId, any()) }
                }
            }
        }

        Given("에셋 카테고리 수정 요청을 할 때") {
            When("수정 요청한 코드를 가진 카테고리가 이미 존재한다면") {
                val categoryId = 1L
                val existingCategory = dummyAssetCategory(id = categoryId, code = "OLD_CODE")
                val request = AssetCategoryUpdateRequest(name = "카테고리", code = "NEW_CODE")

                every { assetCategoryRepository.findByIdOrNull(categoryId) } returns existingCategory
                every { assetCategoryRepository.existsByCode(request.code) } returns true

                val exception =
                    shouldThrow<CustomException> {
                        service.updateAssetCategory(categoryId, request)
                    }

                Then("DUPLICATE_ASSET_CATEGORY_CODE 예외가 발생한다.") {
                    val expectedMessage =
                        ErrorCode.DUPLICATE_ASSET_CATEGORY_CODE
                            .getMessage()
                            .format(request.code)

                    exception.message shouldBe expectedMessage

                    verify(exactly = 1) { assetCategoryRepository.findByIdOrNull(categoryId) }
                    verify(exactly = 1) { assetCategoryRepository.existsByCode(request.code) }
                }
            }

            When("수정 요청한 카테고리가 존재하지 않다면") {
                val invalidCategoryId = 999L
                val request = AssetCategoryUpdateRequest(name = "카테고리", code = "CODE")

                every { assetCategoryRepository.findByIdOrNull(invalidCategoryId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        service.updateAssetCategory(invalidCategoryId, request)
                    }

                Then("NOT_FOUND_CATEGORY 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_CATEGORY.getMessage().format(invalidCategoryId)
                    verify(exactly = 1) { assetCategoryRepository.findByIdOrNull(invalidCategoryId) }
                }
            }

            When("정상적으로 카테고리 수정이 성공하면") {
                val categoryId = 1L
                val category = dummyAssetCategory(id = categoryId, categoryName = "이전 이름", code = "OLD_CODE")
                val request = AssetCategoryUpdateRequest(name = "새 이름", code = "NEW_CODE")

                every { assetCategoryRepository.findByIdOrNull(categoryId) } returns category
                every { assetCategoryRepository.existsByCode(request.code) } returns false

                service.updateAssetCategory(categoryId, request)

                Then("카테고리의 이름과 코드가 요청된 값으로 변경되어야 한다") {
                    category.code shouldBe "NEW_CODE"
                    category.name shouldBe request.name
                }
            }

            When("썸네일 파일 ID가 변경되면") {
                val categoryId = 1L
                val oldThumbnailId = 10L
                val newThumbnailId = 20L
                val category = dummyAssetCategory(id = categoryId, code = "CODE", iconFileId = oldThumbnailId)
                val request =
                    AssetCategoryUpdateRequest(
                        name = "카테고리",
                        code = "CODE",
                        thumbnailFileId = newThumbnailId,
                    )
                val fileEntity = FileEntity(originalFileName = "new-icon.png", filePath = "path", contentType = "image/png")

                every { assetCategoryRepository.findByIdOrNull(categoryId) } returns category
                every { assetCategoryRepository.existsByCode(request.code) } returns false
                every { fileService.finalizeUpload(newThumbnailId, any()) } returns fileEntity

                service.updateAssetCategory(categoryId, request)

                Then("새로운 파일이 finalize되고 카테고리의 iconFileId가 업데이트된다.") {
                    category.iconFileId shouldBe newThumbnailId
                    verify(exactly = 1) { fileService.finalizeUpload(newThumbnailId, any()) }
                }
            }

            When("썸네일 파일 ID를 null로 변경하면") {
                val categoryId = 1L
                val oldThumbnailId = 10L
                val category = dummyAssetCategory(id = categoryId, code = "CODE", iconFileId = oldThumbnailId)
                val request = AssetCategoryUpdateRequest(name = "카테고리", code = "CODE")

                every { assetCategoryRepository.findByIdOrNull(categoryId) } returns category
                every { assetCategoryRepository.existsByCode(request.code) } returns false

                service.updateAssetCategory(categoryId, request)

                Then("카테고리의 iconFileId가 null로 변경된다.") {
                    category.iconFileId.shouldBeNull()
                    verify(exactly = 0) { fileService.finalizeUpload(any(), any()) }
                }
            }
        }

        Given("에셋 카테고리 삭제 요청을 할 때") {
            When("에셋 카테고리 ID가 유효하지 않다면") {
                val invalidCategoryId = 999L

                every { assetCategoryRepository.findByIdOrNull(invalidCategoryId) } returns null

                val exception =
                    shouldThrow<CustomException> {
                        service.deleteAssetCategory(invalidCategoryId)
                    }

                Then("NOT_FOUND_CATEGORY 예외가 발생한다.") {
                    exception.message shouldBe ErrorCode.NOT_FOUND_CATEGORY.getMessage().format(invalidCategoryId)
                    verify(exactly = 1) { assetCategoryRepository.findByIdOrNull(invalidCategoryId) }
                    verify(exactly = 0) { assetCategoryRepository.delete(any()) }
                }
            }

            When("카테고리에 에셋이 있다면") {
                val categoryId = 1L
                val category = dummyAssetCategory(id = categoryId)
                val asset = asset.dummyAsset(category = category)
                category.assets.add(asset)

                every { assetCategoryRepository.findByIdOrNull(categoryId) } returns category

                val exception =
                    shouldThrow<CustomException> {
                        service.deleteAssetCategory(categoryId)
                    }

                Then("ASSET_CATEGORY_HAS_ASSET 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.ASSET_CATEGORY_HAS_ASSET
                    verify(exactly = 1) { assetCategoryRepository.findByIdOrNull(categoryId) }
                    verify(exactly = 0) { assetCategoryRepository.delete(any()) }
                }
            }

            When("카테고리에 자식 카테고리가 있다면") {
                val categoryId = 1L
                val parentCategory = dummyAssetCategory(id = categoryId)
                val childCategory = dummyAssetCategory(id = 2L)
                childCategory.parent = parentCategory
                parentCategory.children.add(childCategory)

                every { assetCategoryRepository.findByIdOrNull(categoryId) } returns parentCategory

                val exception =
                    shouldThrow<CustomException> {
                        service.deleteAssetCategory(categoryId)
                    }

                Then("CATEGORY_HAS_CHILDREN 예외가 발생한다.") {
                    exception.errorCode shouldBe ErrorCode.CATEGORY_HAS_CHILDREN
                    verify(exactly = 1) { assetCategoryRepository.findByIdOrNull(categoryId) }
                    verify(exactly = 0) { assetCategoryRepository.delete(any()) }
                }
            }

            When("정상적으로 카테고리 삭제가 성공하면") {
                val categoryId = 1L
                val category = dummyAssetCategory(id = categoryId)

                every { assetCategoryRepository.findByIdOrNull(categoryId) } returns category
                every { assetCategoryRepository.delete(category) } just runs

                service.deleteAssetCategory(categoryId)

                Then("카테고리가 삭제된다.") {
                    verify(exactly = 1) { assetCategoryRepository.findByIdOrNull(categoryId) }
                    verify(exactly = 1) { assetCategoryRepository.delete(category) }
                }
            }
        }

        Given("카테고리 깊이 조회 요청을 할 때") {
            When("정상적으로 호출이 되면") {
                val result = service.getCategoryDepth()

                Then("MAX_DEPTH를 반환한다.") {
                    result shouldBe CategoryDepthResponse(com.pluxity.asset.entity.AssetCategory.MAX_DEPTH)
                }
            }
        }
    })
