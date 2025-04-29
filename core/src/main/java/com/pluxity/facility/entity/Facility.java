package com.pluxity.facility.entity;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "facility")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Facility extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "drawing_file_id")
    private Long drawingFileId;
    
    @Column(name = "thumbnail_file_id")
    private Long thumbnailFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FacilityCategory category;

    @Builder
    public Facility(String name,
                    String description,
                    Long drawingFileId,
                    Long thumbnailFileId,
                    FacilityCategory category) {
        this.name = name;
        this.description = description;
        this.drawingFileId = drawingFileId;
        this.thumbnailFileId = thumbnailFileId;
        this.category = category;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void updateFileId(Long drawingFileId) {
        this.drawingFileId = drawingFileId;
    }

    public void updateThumbnailId(Long thumbnailFileId) {
        this.thumbnailFileId = thumbnailFileId;
    }

    public void assignCategory(FacilityCategory category) {
        this.category = category;
    }
}