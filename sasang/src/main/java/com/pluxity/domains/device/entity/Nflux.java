package com.pluxity.domains.device.entity;

import com.pluxity.asset.entity.Asset;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.feature.entity.Feature;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "nflux")
@DiscriminatorValue("nflux")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nflux extends Device {

    @Column(name = "code", unique = true, nullable = false)
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
            DeviceCategory category, Asset asset, String name, String code, String description) {
        return Nflux.builder()
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
            String newDescription) {

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

    public boolean hasCode() {
        return this.code != null && !this.code.trim().isEmpty();
    }

    public void validateCodeExists() {
        if (!hasCode()) {
            throw new CustomException("Device code is required", HttpStatus.BAD_REQUEST, "장치 코드는 필수입니다.");
        }
    }

    @Override
    public String getDeviceCode() {
        return this.code;
    }
}
