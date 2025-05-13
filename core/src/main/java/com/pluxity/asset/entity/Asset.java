package com.pluxity.asset.entity;

import com.pluxity.asset.constant.AssetType;
import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_set_id")
    private AssetSet assetSet;


    @Builder
    public Asset(Long id, AssetType type, String name, Long fileId) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.fileId = fileId;
    }

    public void changeAssetSet(AssetSet assetSet) {
        if (this.assetSet != null) {
            this.assetSet.getAssets().remove(this);
        }

        this.assetSet = assetSet;

        if (assetSet != null && !assetSet.getAssets().contains(this)) {
            assetSet.addAsset(this);
        }
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