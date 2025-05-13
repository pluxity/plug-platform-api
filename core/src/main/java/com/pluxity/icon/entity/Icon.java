package com.pluxity.icon.entity;

import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.entity.BaseEntity;
import com.pluxity.icon.dto.IconCreateRequest;
import com.pluxity.icon.dto.IconUpdateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.io.File.separator;

@Entity
@Table(name = "icon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Icon extends BaseEntity {

    public static final String ICONS_PATH = "icons";

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "file_id")
    private Long fileId;

    @Builder
    public Icon(String name, Long fileId) {
        this.name = name;
        this.fileId = fileId;
    }

    public static Icon create(IconCreateRequest request) {
        return Icon.builder()
                .name(request.name())
                .build();
    }

    public void update(IconUpdateRequest request) {
        if (request.name() != null) {
            this.name = request.name();
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

    public String getIconFilePath() {
        return ICONS_PATH + separator + fileId + separator + name;
    }

    public boolean hasFile() {
        return this.fileId != null;
    }
}
