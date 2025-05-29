package com.pluxity.domains.device.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nflux")
@DiscriminatorValue("nflux")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nflux extends Device {

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Builder
    public Nflux(
            Feature feature, DeviceCategory category, String name, String code, String description) {
        super(feature, category);
        updateName(name);
        this.code = code;
        this.description = description;
    }

    public static Nflux create(
            Feature feature,
            DeviceCategory category,
            Asset asset,
            String name,
            String code,
            String description) {
        return Nflux.builder()
                .feature(feature)
                .category(category)
                .name(name)
                .code(code)
                .description(description)
                .build();
    }

    public void update(
            DeviceCategory newCategory,
            Asset newAsset,
            String newName,
            String newCode,
            String newDescription,
            FeatureUpdateRequest featureUpdateRequest) {
        if (featureUpdateRequest != null) {
            this.getFeature().update(featureUpdateRequest);
        }

        if (newCategory != null) {
            updateCategory(newCategory);
        }

        if (newName != null) {
            updateName(newName);
        }
        if (newCode != null) {
            this.code = newCode;
        }
        if (newDescription != null) {
            this.description = newDescription;
        }
    }

    public void updateCode(String code) {
        this.code = code;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
