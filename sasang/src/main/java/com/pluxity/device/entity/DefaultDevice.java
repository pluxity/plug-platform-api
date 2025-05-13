package com.pluxity.device.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.facility.entity.Station;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device")
@DiscriminatorValue("DEFAULT_DEVICE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefaultDevice extends Device {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private DeviceCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset2d_id")
    private Asset asset2d;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Builder
    public DefaultDevice(Feature feature, Asset asset, DeviceCategory category, Station station, Asset asset2d, String name, String code, String description) {
        super(feature, asset);
        this.category = category;
        this.station = station;
        this.asset2d = asset2d;
        updateAsset(asset2d);
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public static DefaultDevice create(
            Feature feature,
            DeviceCategory category,
            Station station,
            Asset asset2d,
            Asset asset3d,
            String name,
            String code,
            String description
    ) {
        return DefaultDevice.builder()
                .feature(feature)
                .category(category)
                .station(station)
                .asset(asset2d)
                .asset(asset3d)
                .name(name)
                .code(code)
                .description(description)
                .build();
    }

    public void update(
            DeviceCategory newCategory,
            Station newStation,
            Asset newAsset2d,
            Asset newAsset3d,
            String newName,
            String newCode,
            String newDescription,
            FeatureUpdateRequest featureUpdateRequest
    ) {
        if (featureUpdateRequest != null) {
            this.getFeature().update(featureUpdateRequest);
        }

        if (newCategory != null) {
            if (this.category != null) {
                this.category.removeDevice(this);
            }
            this.category = newCategory;
            this.category.addDevice(this);
        }

        if (newStation != null) {
            this.station = newStation;
        }

        if (newAsset2d != null) {
            updateAsset(asset2d);
        }

        if (newAsset3d != null) {
            changeAsset(newAsset3d);
        }
        
        if (newName != null) {
            this.name = newName;
        }
        if (newCode != null) {
            this.code = newCode;
        }
        if (newDescription != null) {
            this.description = newDescription;
        }
    }


    public void updateCategory(DeviceCategory category) {
        if (this.category != null) {
            this.category.removeDevice(this);
        }
        this.category = category;
        if (category != null) {
            category.addDevice(this);
        }
    }

    public void updateStation(Station station) {
        this.station = station;
    }

    public void updateAsset(Asset asset) {
        this.asset2d = asset;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateCode(String code) {
        this.code = code;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

}