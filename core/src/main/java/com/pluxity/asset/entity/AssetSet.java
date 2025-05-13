package com.pluxity.asset.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asset_set")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "assetSet", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Asset> assets = new ArrayList<>();

    public void addAsset(Asset asset) {
        if (!this.assets.contains(asset)) {
            this.assets.add(asset);
        }

        if (asset.getAssetSet() != this) {
            asset.changeAssetSet(this);
        }
    }

    public void removeAsset(Asset asset) {
        if (this.assets.contains(asset)) {
            this.assets.remove(asset);
            if (asset.getAssetSet() == this) {
                asset.changeAssetSet(null);
            }
        }
    }
}
