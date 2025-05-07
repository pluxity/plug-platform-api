package com.pluxity.facility.entity;

import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.entity.BaseEntity;
import com.pluxity.facility.domain.FacilityType;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", nullable = false)
    private FacilityType facilityType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FacilityCategory category;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drawing_file_id")
    private FileEntity drawingFile;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_file_id")
    private FileEntity thumbnailFile;
    
    public Facility(String name, String description, FacilityType facilityType) {
        this.name = name;
        this.description = description;
        this.facilityType = facilityType;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateDrawingFile(FileEntity drawingFile) {
        this.drawingFile = drawingFile;
    }

    public void updateThumbnailFile(FileEntity thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public void assignCategory(FacilityCategory category) {
        this.category = category;
    }
}