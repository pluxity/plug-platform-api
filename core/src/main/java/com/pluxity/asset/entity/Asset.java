package com.pluxity.asset.entity;

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

    private String name;

    private Long fileId;

    @Builder
    public Asset(Long id, String name, Long fileId) {
        this.id = id;
        this.name = name;
        this.fileId = fileId;
    }

    public static Asset create(AssetCreateRequest request) {
        return Asset.builder()
                .name(request.name())
                .build();
    }

    public void update(AssetUpdateRequest request) {
        if (request.name() != null) {
            this.name = request.name();
        }
    }

    public void update(String name, String type) {
        if (name != null) {
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

}
