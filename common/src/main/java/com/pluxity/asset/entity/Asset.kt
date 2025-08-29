package com.pluxity.asset.entity

import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.file.entity.FileEntity
import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "asset")
@EntityListeners(AuditingEntityListener::class)
class Asset(
    name: String? = null,
    code: String? = null,
    fileId: Long? = null,
    thumbnailFileId: Long? = null,
    category: AssetCategory? = null,
) : BaseEntity() {
    companion object {
        const val ASSETS_PATH: String = "assets"

        @JvmStatic
        fun create(request: AssetCreateRequest): Asset =
            Asset(
                name = request.name,
                code = request.code,
                thumbnailFileId = request.thumbnailFileId,
            )

        fun builder() = Builder()
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "name", unique = true, nullable = false, length = 50)
    var name: String? = name

    @Column(name = "code", unique = true, nullable = false, length = 50)
    var code: String? = code

    @Column(name = "file_id")
    var fileId: Long? = fileId

    @Column(name = "thumbnail_file_id")
    var thumbnailFileId: Long? = thumbnailFileId

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: AssetCategory? = category
        private set

    init {
        if (this.category != null) {
            this.category!!.addAsset(this)
        }
    }

    fun update(request: com.pluxity.asset.dto.AssetUpdateRequest) {
        this.name = request.name
        this.code = request.code
        request.thumbnailFileId?.let { this.thumbnailFileId = it }
    }

    fun update(name: String?) {
        name?.let { this.name = it }
    }

    fun updateFileEntity(fileEntity: FileEntity?) {
        fileEntity?.let { this.fileId = it.id }
    }

    fun updateThumbnailFileEntity(fileEntity: FileEntity?) {
        fileEntity?.let { this.thumbnailFileId = it.id }
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

    fun getAssetFilePath(): String = "$ASSETS_PATH/${this.id}/"

    fun getThumbnailFilePath(): String = "$ASSETS_PATH/${this.id}/thumbnail/"

    fun hasFile(): Boolean = this.fileId != null

    fun hasThumbnail(): Boolean = this.thumbnailFileId != null

    fun clearAllRelations() {
        updateCategory(null)
    }

    class Builder {
        private var name: String? = null
        private var code: String? = null
        private var fileId: Long? = null
        private var thumbnailFileId: Long? = null
        private var category: AssetCategory? = null

        fun name(name: String) = apply { this.name = name }

        fun code(code: String) = apply { this.code = code }

        fun fileId(fileId: Long?) = apply { this.fileId = fileId }

        fun thumbnailFileId(thumbnailFileId: Long?) = apply { this.thumbnailFileId = thumbnailFileId }

        fun category(category: AssetCategory?) = apply { this.category = category }

        fun build(): Asset =
            Asset(
                name = name,
                code = code,
                fileId = fileId,
                thumbnailFileId = thumbnailFileId,
                category = category,
            )
    }
}
