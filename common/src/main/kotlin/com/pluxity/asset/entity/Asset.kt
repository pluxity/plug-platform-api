package com.pluxity.asset.entity

import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.file.entity.FileEntity
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.global.exception.CustomException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "asset")
@EntityListeners(AuditingEntityListener::class)
class Asset(
    @Column(name = "name", unique = true, nullable = false, length = 50)
    var name: String,
    @Column(name = "code", unique = true, nullable = false, length = 50)
    var code: String,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: AssetCategory? = null,
    @Column(name = "file_id")
    var fileId: Long? = null,
    @Column(name = "thumbnail_file_id")
    var thumbnailFileId: Long? = null,
) : IdentityIdEntity() {
    companion object {
        const val ASSETS_PATH: String = "assets"

        fun create(request: AssetCreateRequest): Asset {
            require(request.name.isNotBlank()) {
                "Asset name cannot be null or blank"
            }
            require(request.code.isNotBlank()) {
                "Asset code cannot be null or blank"
            }

            return Asset(
                name = request.name,
                code = request.code,
                fileId = request.fileId,
                thumbnailFileId = request.thumbnailFileId,
            )
        }
    }

    init {
        category?.addAsset(this)
    }

    fun update(request: AssetUpdateRequest) {
        validateUpdateRequest(request)
        this.name = request.name
        this.code = request.code
        request.thumbnailFileId?.let { this.thumbnailFileId = it }
    }

    fun update(name: String?) {
        name?.takeIf { it.isNotBlank() }?.let { this.name = it }
    }

    private fun validateUpdateRequest(request: AssetUpdateRequest) {
        require(request.name.isNotBlank()) {
            "Asset name cannot be null or blank"
        }
        require(request.code.isNotBlank()) {
            "Asset code cannot be null or blank"
        }
    }

    fun updateFileEntity(fileEntity: FileEntity?) {
        fileEntity?.let { this.fileId = it.id }
    }

    fun updateThumbnailFileEntity(fileEntity: FileEntity?) {
        fileEntity?.let { this.thumbnailFileId = it.id }
    }

    fun updateCategory(category: AssetCategory?) {
        this.category?.removeAsset(this)
        this.category = category
        category?.addAsset(this)
    }

    fun assignCategory(category: AssetCategory) {
        updateCategory(category)
    }

    fun removeCategory() {
        require(this.category != null) {
            throw CustomException(ErrorCode.NOT_EXIST_ASSET_CATEGORY, this.id)
        }
        updateCategory(null)
    }

    fun getAssetFilePath(): String = "$ASSETS_PATH/${requireNotNull(this.id)}/"

    fun getThumbnailFilePath(): String = "$ASSETS_PATH/${requireNotNull(this.id)}/thumbnail/"

    fun hasFile(): Boolean = this.fileId != null

    fun hasThumbnail(): Boolean = this.thumbnailFileId != null

    fun clearAllRelations() {
        this.category?.removeAsset(this)
        this.category = null
    }
}
