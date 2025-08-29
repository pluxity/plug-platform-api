package com.pluxity.asset.entity

import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.asset.entity.AssetCategory.code
import com.pluxity.file.entity.FileEntity
import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener::class)
class Asset @Builder constructor(
    @field:Column(name = "name", unique = true, nullable = false, length = 50) private var name: String?,
    @field:Column(
        name = "code",
        unique = true,
        nullable = false,
        length = 50
    ) private var code: String?,
    @field:Column(name = "file_id") private var fileId: Long?,
    @field:Column(name = "thumbnail_file_id") private var thumbnailFileId: Long?,
    @field:JoinColumn(
        name = "category_id"
    ) @field:ManyToOne(fetch = FetchType.LAZY) private var category: AssetCategory?
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    init {
        if (this.category != null) {
            this.category!!.addAsset(this)
        }
    }

    fun update(request: AssetUpdateRequest) {
        if (request.name != null) {
            this.name = request.name
        }
        if (request.code != null) {
            this.code = request.code
        }
        if (request.thumbnailFileId != null) {
            this.thumbnailFileId = request.thumbnailFileId
        }
    }

    fun update(name: String?) {
        if (name != null) {
            this.name = name
        }
    }

    fun updateFileEntity(fileEntity: FileEntity?) {
        if (fileEntity != null) {
            this.fileId = fileEntity.getId()
        }
    }

    fun updateThumbnailFileEntity(fileEntity: FileEntity?) {
        if (fileEntity != null) {
            this.thumbnailFileId = fileEntity.getId()
        }
    }

    fun updateCategory(category: AssetCategory?) {
        if (this.category != null) {
            this.category!!.removeAsset(this)
        }
        this.category = category
        if (category != null) {
            category.addAsset(this)
        }
    }

    val assetFilePath: String
        get() = ASSETS_PATH + "/" + this.id + "/"

    val thumbnailFilePath: String
        get() = ASSETS_PATH + "/" + this.id + "/thumbnail/"

    fun hasFile(): Boolean {
        return this.fileId != null
    }

    fun hasThumbnail(): Boolean {
        return this.thumbnailFileId != null
    }

    fun clearAllRelations() {
        // 카테고리 연관관계 제거
        this.updateCategory(null)
    }

    companion object {
        const val ASSETS_PATH: String = "assets"

        fun create(request: AssetCreateRequest): Asset? {
            return Asset.builder()
                .name(request.name)
                .code(request.code)
                .thumbnailFileId(request.thumbnailFileId)
                .build()
        }
    }
}
