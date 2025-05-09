package com.pluxity.device.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.feature.entity.Feature;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor
public abstract class Device extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    protected Device(Asset asset, Feature feature) {
        this.asset = asset;
        if (this.asset != null) {
            this.asset.addDevice(this);
        }
        this.feature = feature;
        if (this.feature != null) {
            this.feature.addDevice(this);
        }
    }

    public void changeAsset(Asset newAsset) {
        if (this.asset != null) {
            this.asset.removeDevice(this);
        }
        this.asset = newAsset;
        if (newAsset != null) {
            newAsset.addDevice(this);
        }
    }

    public void changeFeature(Feature newFeature) {
        if (this.feature != null) {
            this.feature.removeDevice(this);
        }
        this.feature = newFeature;
        if (newFeature != null) {
            newFeature.addDevice(this);
        }
    }
}