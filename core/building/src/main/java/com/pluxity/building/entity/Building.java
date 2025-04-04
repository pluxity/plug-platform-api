package com.pluxity.building.entity;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "building")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Building extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "file_id")
    private Long fileId;
    
    @Column(name = "thumbnail_id")
    private Long thumbnailId;

    @Builder
    public Building(String name, String description, Long fileId, Long thumbnailId) {
        this.name = name;
        this.description = description;
        this.fileId = fileId;
        this.thumbnailId = thumbnailId;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public void updateFileId(Long fileId) {
        this.fileId = fileId;
    }
    
    public void updateThumbnailId(Long thumbnailId) {
        this.thumbnailId = thumbnailId;
    }
}