package com.pluxity.facility.entity;

import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "facility")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "facility_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public abstract class Facility extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FacilityCategory category;

    @Column(name = "drawing_file_id")
    private Long drawingFileId;

    @Column(name = "thumbnail_file_id")
    private Long thumbnailFileId;

    public Facility(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateDrawingFileId(FileEntity drawingFile) {
        this.drawingFileId = drawingFile.getId();
    }

    public void updateThumbnailFileId(FileEntity thumbnailFile) {
        this.thumbnailFileId = thumbnailFile.getId();
    }

    public void assignCategory(FacilityCategory category) {
        this.category = category;
    }

    public void update(Facility facility) {
        if (facility.name != null) {
            this.name = facility.name;
        }
        if (facility.description != null) {
            this.description = facility.description;
        }
    }
}