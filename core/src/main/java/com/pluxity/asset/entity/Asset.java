package com.pluxity.asset.entity;

import com.pluxity.asset.constant.AssetType;
import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.device.entity.Device;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "asset")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Asset extends BaseEntity {

    public static final String ASSETS_PATH = "assets";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AssetType type;

    private String name;

    private Long fileId;

    @OneToMany(mappedBy = "asset", cascade = CascadeType.PERSIST)
    private final List<Device> devices = new ArrayList<>();

    @Builder
    public Asset(Long id, AssetType type, String name, Long fileId) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.fileId = fileId;
    }

    public void addDevice(Device device) {
        if (device != null && !this.devices.contains(device)) {
            this.devices.add(device);
            if (device.getAsset() != this) {
                device.changeAsset(this);
            }
        }
    }

    public void removeDevice(Device device) {
        if (device != null && this.devices.contains(device)) {
            this.devices.remove(device);
            if (device.getAsset() == this) {
                device.changeAsset(null);
            }
        }
    }

    public List<Device> getDevices() {
        return Collections.unmodifiableList(devices);
    }


    public static Asset create(AssetCreateRequest request) {
        return Asset.builder()
                .type(AssetType.valueOf(request.type()))
                .name(request.name())
                .build();
    }

    public void update(AssetUpdateRequest request) {
        if(request.type() != null) {
            this.type = AssetType.valueOf(request.type());
        }
        if(request.name() != null) {
            this.name = request.name();
        }
    }

    public void update(String name, String type) {
        if(type != null) {
            this.type = AssetType.valueOf(type);
        }
        if(name != null) {
            this.name = name;
        }
    }

    public void updateFileId(Long fileId) {
        this.fileId = fileId;
    }

    public void updateFileEntity(FileEntity fileEntity) {
        if (fileEntity != null) {
            this.fileId = fileEntity.getId();
        }
    }

    public String getAssetFilePath() {
        return ASSETS_PATH + "/" + this.id + "/";
    }

    public boolean hasFile() {
        return this.fileId != null;
    }

    public boolean isTwoDimensional() {
        return this.type == AssetType.TWO_DIMENSION;
    }

    public boolean isThreeDimensional() {
        return this.type == AssetType.THREE_DIMENSION;
    }
}