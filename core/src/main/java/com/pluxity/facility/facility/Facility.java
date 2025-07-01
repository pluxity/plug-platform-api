package com.pluxity.facility.facility;

import com.pluxity.facility.category.FacilityCategory;
import com.pluxity.feature.entity.Feature;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "facility")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@SoftDelete
public class Facility extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FacilityCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", nullable = false)
    private FacilityType type;

    @Column(name = "code", unique = true, length = 10)
    private String code;

    @Audited
    @Column(name = "drawing_file_id")
    private Long drawingFileId;

    @Column(name = "thumbnail_file_id")
    private Long thumbnailFileId;

    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "history_comment")
    private String historyComment;

    @OneToMany(mappedBy = "facility")
    private List<Feature> features = new ArrayList<>();

    public Facility(FacilityType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public Facility(FacilityType type, String name, String code, String description,
        String historyComment) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.description = description;
        this.historyComment = historyComment;
    }

    public void updateDrawingFileId(FileEntity drawingFile) {
        this.drawingFileId = drawingFile.getId();
    }

    public void updateThumbnailFileId(FileEntity thumbnailFile) {
        this.thumbnailFileId = thumbnailFile.getId();
    }

    public void updateDrawingFileId(Long drawingFileId) {
        this.drawingFileId = drawingFileId;
    }

    public void updateThumbnailFileId(Long thumbnailFileId) {
        this.thumbnailFileId = thumbnailFileId;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateCode(String code) {
        this.code = code;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateHistoryComment(String historyComment) {
        this.historyComment = historyComment;
    }

    public void assignCategory(FacilityCategory category) {
        this.category = category;
    }

    public void addFeature(Feature feature) {
        if (!this.features.contains(feature)) {
            this.features.add(feature);
        }
    }

    public void removeFeature(Feature feature) {
        this.features.remove(feature);
    }
}
