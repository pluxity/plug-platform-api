package com.pluxity.asset.entity

import com.pluxity.category.entity.Category
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Table(name = "asset_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class AssetCategory @Builder constructor(name: String?, code: String?, iconFileId: Long?) : Category<AssetCategory?>() {
    @OneToMany(mappedBy = "category")
    private val assets: MutableList<Asset?> = ArrayList<Asset?>()

    @Column(name = "code", unique = true, length = 50)
    private var code: String?

    @Column(name = "icon_file_id")
    private var iconFileId: Long?

    init {
        this.name = name
        this.code = code
        this.iconFileId = iconFileId
    }

    fun updateIconFileId(iconFileId: Long?) {
        this.iconFileId = iconFileId
    }

    fun updateCode(code: String?) {
        this.code = code
    }

    fun addAsset(asset: Asset?) {
        if (asset != null && !this.assets.contains(asset)) {
            this.assets.add(asset)
        }
    }

    fun removeAsset(asset: Asset?) {
        if (asset != null) {
            this.assets.remove(asset)
        }
    }

    override fun getMaxDepth(): Int {
        return 1
    }
}
