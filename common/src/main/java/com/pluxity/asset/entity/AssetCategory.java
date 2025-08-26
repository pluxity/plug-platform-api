package com.pluxity.asset.entity;

import com.pluxity.category.entity.Category;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetCategory extends Category<AssetCategory> {

    @OneToMany(mappedBy = "category")
    private final List<Asset> assets = new ArrayList<>();

    @Column(name = "code", unique = true, length = 50)
    private String code;

    @Column(name = "icon_file_id")
    private Long iconFileId;

    @Builder
    public AssetCategory(String name, String code, Long iconFileId) {
        this.name = name;
        this.code = code;
        this.iconFileId = iconFileId;
    }

    public void updateIconFileId(Long iconFileId) {
        this.iconFileId = iconFileId;
    }

    public void updateCode(String code) {
        this.code = code;
    }

    public void addAsset(Asset asset) {
        if (asset != null && !this.assets.contains(asset)) {
            this.assets.add(asset);
        }
    }

    public void removeAsset(Asset asset) {
        if (asset != null) {
            this.assets.remove(asset);
        }
    }

    @Override
    public int getMaxDepth() {
        return 1;
    }
}
