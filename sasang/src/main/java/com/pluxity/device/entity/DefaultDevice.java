package com.pluxity.device.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.facility.entity.Facility;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.icon.entity.Icon;
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
    @JoinColumn(name = "icon_id")
    private Icon icon;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Builder
    public DefaultDevice(
            Feature feature,
            Asset asset,
            DeviceCategory category,
            Facility facility,
            Icon icon,
            String name,
            String code,
            String description) {
        super(feature, asset, category, facility);
        this.icon = icon;
        updateName(name);
        this.code = code;
        this.description = description;
    }

    public static DefaultDevice create(
            Feature feature,
            DeviceCategory category,
            Facility facility,
            Icon icon,
            Asset asset,
            String name,
            String code,
            String description) {
        return DefaultDevice.builder()
                .feature(feature)
                .category(category)
                .facility(facility)
                .icon(icon)
                .asset(asset)
                .name(name)
                .code(code)
                .description(description)
                .build();
    }

    public void update(
            DeviceCategory newCategory,
            Facility newFacility,
            Icon newIcon,
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

        if (newFacility != null) {
            updateFacility(newFacility);
        }

        if (newIcon != null) {
            this.icon = newIcon;
        }

        if (newAsset != null) {
            changeAsset(newAsset);
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

    public void updateIcon(Icon icon) {
        this.icon = icon;
    }
}
