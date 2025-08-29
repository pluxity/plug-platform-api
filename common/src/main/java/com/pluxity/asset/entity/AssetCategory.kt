package com.pluxity.asset.entity

import com.pluxity.category.entity.Category
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "asset_category")
class AssetCategory(
    code: String? = null,
    iconFileId: Long? = null,
) : Category<AssetCategory>() {
    @Column(name = "code", unique = true, length = 50)
    var code: String? = code

    @Column(name = "icon_file_id")
    var iconFileId: Long? = iconFileId

    @OneToMany(mappedBy = "category")
    val assets: MutableList<Asset> = mutableListOf()

    fun updateIconFileId(iconFileId: Long?) {
        this.iconFileId = iconFileId
    }

    fun updateCode(code: String?) {
        this.code = code
    }

    fun addAsset(asset: Asset?) {
        asset?.let {
            if (!this.assets.contains(it)) {
                this.assets.add(it)
            }
        }
    }

    fun removeAsset(asset: Asset?) {
        asset?.let { this.assets.remove(it) }
    }

    override fun getMaxDepth(): Int = 1

    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var name: String? = null
        private var code: String? = null
        private var iconFileId: Long? = null

        fun name(name: String) = apply { this.name = name }

        fun code(code: String) = apply { this.code = code }

        fun iconFileId(iconFileId: Long?) = apply { this.iconFileId = iconFileId }

        fun build(): AssetCategory {
            val category = AssetCategory(code, iconFileId)
            name?.let { category.name = it }
            return category
        }
    }
}
