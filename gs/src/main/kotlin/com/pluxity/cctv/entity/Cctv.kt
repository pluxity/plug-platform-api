package com.pluxity.cctv.entity

import com.pluxity.cctv.dto.CctvUpdateRequest
import com.pluxity.feature.entity.Feature
import com.pluxity.global.entity.BaseEntity
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.Permissible
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
class Cctv(
    @Id
    var id: String,
    // DB 컬럼은 name 유지
    var name: String = "",
    @Column(length = 1000)
    var url: String?,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "feature_id")
    var feature: Feature? = null,
) : BaseEntity(),
    Permissible {
    fun updateCctv(request: CctvUpdateRequest) {
        this.name = request.name
        this.url = request.url
    }

    fun changeFeature(feature: Feature?) {
        this.feature = feature
    }

    override val resourceId: String
        get() = id

    override val resourceType: ResourceType
        get() = ResourceType.CCTV
}
