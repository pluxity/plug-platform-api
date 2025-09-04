package com.pluxity.facility

import com.pluxity.facility.category.FacilityCategory
import com.pluxity.facility.path.FacilityPath
import com.pluxity.feature.entity.Feature
import com.pluxity.file.entity.FileEntity
import com.pluxity.global.entity.BaseEntity
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Permissible
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Getter
import lombok.NoArgsConstructor
import org.hibernate.annotations.SoftDelete
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "facility")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "facility_type")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener::class)
@SoftDelete
abstract class Facility : BaseEntity, Permissible {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private var category: FacilityCategory? = null

    @Column(name = "code", length = 50)
    private var code: String?

    @Column(name = "drawing_file_id")
    private var drawingFileId: Long? = null

    @Column(name = "thumbnail_file_id")
    private var thumbnailFileId: Long? = null

    @Column(name = "name", nullable = false, length = 50)
    private var name: String?

    @Column(name = "description")
    private var description: String? = null

    @Column(name = "history_comment")
    private var historyComment: String? = null

    @Embedded
    private var position: FacilityPosition? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", insertable = false, updatable = false)
    private var facilityType: FacilityType? = null

    @OneToMany(mappedBy = "facility")
    private val features: MutableList<Feature?> = ArrayList<Feature?>()

    @OneToMany(mappedBy = "facility")
    private val paths: MutableList<FacilityPath?> = ArrayList<FacilityPath?>()

    protected constructor(name: String?, description: String?) : this(name, null, description, null)

    constructor(name: String?, description: String?, historyComment: String?) : this(name, null, description, historyComment)

    protected constructor(name: String?, code: String?, drawingFileId: Long?, thumbnailFileId: Long?) {
        this.code = code
        this.name = name
        this.drawingFileId = drawingFileId
        this.thumbnailFileId = thumbnailFileId
    }

    protected constructor(name: String?, code: String?, description: String?, historyComment: String?) {
        this.code = code
        this.name = name
        this.description = description
        this.historyComment = historyComment
    }

    constructor(name: String?, code: String?, description: String?, drawingFileId: Long?, thumbnailFileId: Long?) {
        this.name = name
        this.code = code
        this.description = description
        this.drawingFileId = drawingFileId
        this.thumbnailFileId = thumbnailFileId
    }

    fun updateDrawingFileId(drawingFile: FileEntity) {
        this.drawingFileId = drawingFile.id
    }

    fun updateThumbnailFileId(thumbnailFile: FileEntity) {
        this.thumbnailFileId = thumbnailFile.id
    }

    fun updateDrawingFileId(drawingFileId: Long?) {
        this.drawingFileId = drawingFileId
    }

    fun updateThumbnailFileId(thumbnailFileId: Long?) {
        this.thumbnailFileId = thumbnailFileId
    }

    fun updateName(name: String?) {
        this.name = name
    }

    fun updateCode(code: String?) {
        this.code = code
    }

    fun updateDescription(description: String?) {
        this.description = description
    }

    fun updateHistoryComment(historyComment: String?) {
        this.historyComment = historyComment
    }

    fun assignCategory(category: FacilityCategory?) {
        this.category = category
    }

    fun addFeature(feature: Feature?) {
        if (!this.features.contains(feature)) {
            this.features.add(feature)
        }
    }

    fun removeFeature(feature: Feature?) {
        this.features.remove(feature)
    }

    fun update(facility: Facility) {
        if (facility.name != null) {
            this.name = facility.name
        }
        if (facility.code != null) {
            this.code = facility.code
        }
        if (facility.description != null) {
            this.description = facility.description
        }
        if (facility.historyComment != null) {
            this.historyComment = facility.historyComment
        }
        if (facility.drawingFileId != null) {
            this.drawingFileId = facility.drawingFileId
        }
        if (facility.thumbnailFileId != null) {
            this.thumbnailFileId = facility.thumbnailFileId
        }
    }

    fun updatePosition(position: FacilityPosition?) {
        this.position = position
    }

    fun updatePosition(lon: Double?, lat: Double?, locationMeta: String?) {
        if (this.position == null) {
            this.position = FacilityPosition()
        }
        this.position!!.merge(lon, lat, locationMeta)
    }

    val resourceId: String
        get() = this.id.toString()

    val resourceType: ResourceType
        get() = ResourceType.FACILITY
}
