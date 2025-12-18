package com.pluxity.label3d

import com.pluxity.feature.entity.Feature
import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "label_3d")
data class Label3D(
    @Id
    var id: String? = null,
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    var feature: Feature,
    @Column(name = "display_text")
    var displayText: String? = null,
) : BaseEntity() {
    fun update(displayText: String?) {
        displayText?.let { this.displayText = it }
    }

    fun requiredId(): String = checkNotNull(this.id) { "Label ID is required" }
}
