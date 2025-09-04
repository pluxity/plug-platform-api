package com.pluxity.asset.entity

import com.pluxity.category.entity.Category
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "asset_category")
class AssetCategory(
    @Column(name = "code", unique = true, length = 50)
    var code: String? = null,
    @Column(name = "icon_file_id")
    var iconFileId: Long? = null,
    var categoryName: String,
) : Category<AssetCategory>(categoryName) {
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

    override val maxDepth: Int
        get() = MAX_DEPTH

    companion object {
        const val MAX_DEPTH = 1
    }
}
