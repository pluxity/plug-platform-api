package asset

import base.entity.withAudit
import base.entity.withId
import com.pluxity.asset.entity.Asset
import com.pluxity.asset.entity.AssetCategory

fun dummyAssetCategory(
    id: Long? = null,
    code: String = "category-code",
    iconFileId: Long? = null,
    categoryName: String = "category-name",
) = AssetCategory(
    code = code,
    iconFileId = iconFileId,
    categoryName = categoryName,
).withId(id).withAudit()

fun dummyAsset(
    id: Long? = null,
    name: String = "asset-name",
    code: String = "asset-code",
    category: AssetCategory = dummyAssetCategory(iconFileId = 1L),
    fileId: Long? = null,
    thumbnailFileId: Long? = null,
) = Asset(
    name = name,
    code = code,
    fileId = fileId,
    thumbnailFileId = thumbnailFileId,
).apply { assignCategory(category) }.withId(id).withAudit()
