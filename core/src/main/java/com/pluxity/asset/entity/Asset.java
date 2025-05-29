package com.pluxity.asset.entity;

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

import java.util.ArrayList;
import java.util.List;

import static java.io.File.separator;

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

    @Column(name = "code", unique = true, length = 3)
    private String code;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "thumbnail_file_id")
    private Long thumbnailFileId;

    @OneToMany(mappedBy = "asset") //Persist ALL 하면 생성할때 id 중복되서 오류 발생 가능
    private final List<Feature> features = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AssetCategory category;

    @Builder
    public Asset(
            String name, String code, Long fileId, Long thumbnailFileId, AssetCategory category) {
        this.name = name;
        this.code = code;
        this.fileId = fileId;
        this.thumbnailFileId = thumbnailFileId;
        this.category = category;
        if (this.category != null) {
            this.category.addAsset(this);
        }
    }

    public static Asset create(AssetCreateRequest request) {
        return Asset.builder()
                .name(request.name())
                .code(request.code())
                .thumbnailFileId(request.thumbnailFileId())
                .build();
    }

    public void update(AssetUpdateRequest request) {
        if (request.name() != null) {
            this.name = request.name();
        }
        if (request.code() != null) {
            this.code = request.code();
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

    public void updateCode(String code) {
        if (code != null) {
            this.code = code;
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

    public void updateCategory(AssetCategory category) {
        if (this.category != null) {
            this.category.removeAsset(this);
        }
        this.category = category;
        if (category != null) {
            category.addAsset(this);
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

    public void addFeature(Feature feature) {
        if (feature != null && !this.features.contains(feature)) {
            this.features.add(feature);
        }
    }

    public void removeFeature(Feature feature) {
        if (feature != null) {
            this.features.remove(feature);
        }
    }
}
