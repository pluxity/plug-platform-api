package com.pluxity.facility

import com.pluxity.facility.category.FacilityCategory
import com.pluxity.facility.path.FacilityPath
import com.pluxity.feature.entity.Feature
import com.pluxity.file.entity.FileEntity
import com.pluxity.global.entity.BaseEntity
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Permissible
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.SoftDelete
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "facility")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "facility_type")
@EntityListeners(AuditingEntityListener::class)
@SoftDelete
abstract class Facility(
    @Column(name = "name", nullable = false, length = 50)
    var name: String,
    @Column(name = "code", length = 50)
    var code: String? = null,
    @Column(name = "description")
    var description: String? = null,
    @Column(name = "history_comment")
    var historyComment: String? = null,
    @Column(name = "drawing_file_id")
    var drawingFileId: Long? = null,
    @Column(name = "thumbnail_file_id")
    var thumbnailFileId: Long? = null,
    @Embedded
    var position: FacilityPosition? = null,
) : BaseEntity(),
    Permissible {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    lateinit var category: FacilityCategory
        private set

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", insertable = false, updatable = false)
    var facilityType: FacilityType? = null
        private set

    @OneToMany(mappedBy = "facility")
    val features: MutableList<Feature> = mutableListOf()

    @OneToMany(mappedBy = "facility")
    val paths: MutableList<FacilityPath> = mutableListOf()

    fun updateCode(code: String?) {
        this.code = code
    }

    fun updateName(name: String) {
        require(name.isNotBlank()) { "시설명은 빈 값일 수 없습니다" }
        this.name = name
    }

    fun updateDescription(description: String?) {
        this.description = description
    }

    fun updateHistoryComment(historyComment: String?) {
        this.historyComment = historyComment
    }

    fun updateDrawingFileId(drawingFileId: Long?) {
        this.drawingFileId = drawingFileId
    }

    fun updateDrawingFile(drawingFile: FileEntity) {
        this.drawingFileId = drawingFile.id
    }

    fun updateThumbnailFileId(thumbnailFileId: Long?) {
        this.thumbnailFileId = thumbnailFileId
    }

    fun updateThumbnailFile(thumbnailFile: FileEntity) {
        this.thumbnailFileId = thumbnailFile.id
    }

    fun updatePosition(position: FacilityPosition?) {
        this.position = position
    }

    fun updatePosition(
        lon: Double?,
        lat: Double?,
        locationMeta: String?,
    ) {
        this.position =
            if (lon != null || lat != null || locationMeta != null) {
                FacilityPosition(lon, lat, locationMeta)
            } else {
                null
            }
    }

    fun assignCategory(category: FacilityCategory) {
        this.category = category
    }

    fun update(other: Facility) {
        this.name = other.name
        this.code = other.code
        this.description = other.description
        this.historyComment = other.historyComment
        this.drawingFileId = other.drawingFileId
        this.thumbnailFileId = other.thumbnailFileId
        this.position = other.position
        if (::category.isInitialized && other::category.isInitialized) {
            this.category = other.category
        }
    }

    override val resourceId: String
        get() = id.toString()

    override val resourceType: ResourceType
        get() = ResourceType.FACILITY
}
