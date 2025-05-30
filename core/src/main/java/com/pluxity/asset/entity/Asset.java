package com.pluxity.asset.entity;

import static java.io.File.separator;

import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.feature.entity.Feature;
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

    @Column(name = "name")
    private String name;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "thumbnail_file_id")
    private Long thumbnailFileId;

    @OneToOne(mappedBy = "asset")
    private Feature feature;

    @Builder
    public Asset(String name, Long fileId, Long thumbnailFileId) {
        this.name = name;
        this.fileId = fileId;
        this.thumbnailFileId = thumbnailFileId;
    }

    public static Asset create(AssetCreateRequest request) {
        return Asset.builder().name(request.name()).thumbnailFileId(request.thumbnailFileId()).build();
    }

    public void update(AssetUpdateRequest request) {
        if (request.name() != null) {
            this.name = request.name();
        }
        if (request.thumbnailFileId() != null) {
            this.thumbnailFileId = request.thumbnailFileId();
        }
    }

    public void update(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public void updateFileId(Long fileId) {
        this.fileId = fileId;
    }

    public void updateThumbnailFileId(Long thumbnailFileId) {
        this.thumbnailFileId = thumbnailFileId;
    }

    public void updateFileEntity(FileEntity fileEntity) {
        if (fileEntity != null) {
            this.fileId = fileEntity.getId();
        }
    }

    public void updateThumbnailFileEntity(FileEntity fileEntity) {
        if (fileEntity != null) {
            this.thumbnailFileId = fileEntity.getId();
        }
    }

    public String getAssetFilePath() {
        return ASSETS_PATH + separator + this.id + separator;
    }

    public String getThumbnailFilePath() {
        return ASSETS_PATH + separator + this.id + separator + "thumbnail" + separator;
    }

    public boolean hasFile() {
        return this.fileId != null;
    }

    public boolean hasThumbnail() {
        return this.thumbnailFileId != null;
    }

    public void changeFeature(Feature feature) {
        this.feature = feature;
    }
}
