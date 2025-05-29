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

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Asset> assets = new ArrayList<>();

    @Column(name = "code", unique = true, length = 10)
    private String code;

    @Column(name = "icon_file_id")
    private Long iconFileId;

    @Builder
    public AssetCategory(String name, String code, AssetCategory parent) {
        this.name = name;
        this.code = code;
        if (parent != null) {
            this.assignToParent(parent);
        } else {
            this.parent = null;
        }
        this.validateDepth();
    }

    @Override
    public int getMaxDepth() {
        return 1;
    }

    public void updateIconFileId(Long iconFileId) {
        this.iconFileId = iconFileId;
    }

    public void updateCode(String code) {
        this.code = code;
    }

    public void updateName(String name) {
        this.name = name;
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
}
