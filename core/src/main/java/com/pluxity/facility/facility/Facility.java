package com.pluxity.facility.facility;

import com.pluxity.facility.category.FacilityCategory;
import com.pluxity.file.entity.FileEntity;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FacilityCategory category;

    @Column(name = "code", unique = true, length = 3)
    private String code;

    @Audited
    @Column(name = "drawing_file_id")
    private Long drawingFileId;

    @Column(name = "thumbnail_file_id")
    private Long thumbnailFileId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "history_comment")
    private String historyComment;

    protected Facility(String name, String description) {
        this(name, null, description, null);
    }

    public Facility(String name, String description, String historyComment) {
        this(name, null, description, historyComment);
    }

    protected Facility(String name, String code, Long drawingFileId, Long thumbnailFileId) {
        this.code = code;
        this.name = name;
        this.drawingFileId = drawingFileId;
        this.thumbnailFileId = thumbnailFileId;
    }

    protected Facility(String name, String code, String description, String historyComment) {
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

    public void update(Facility facility) {
        if (facility.name != null) {
            this.name = facility.name;
        }
        if (facility.code != null) {
            this.code = facility.code;
        }
        if (facility.description != null) {
            this.description = facility.description;
        }
        if (facility.historyComment != null) {
            this.historyComment = facility.historyComment;
        }
    }
}
