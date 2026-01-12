package com.pluxity.patrol.entity

import com.pluxity.facility.Facility
import com.pluxity.feature.entity.Spatial
import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction

@Entity
class Scene(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    var facility: Facility,
    @OneToMany(mappedBy = "scene", cascade = [CascadeType.ALL], orphanRemoval = true)
    var sceneDeviceActions: MutableList<SceneDeviceAction> = mutableListOf(),
    @Column(nullable = false)
    var name: String,
    var description: String? = null,
    var duration: Double? = null,
    @AttributeOverride(
        name = "z",
        column = Column(name = "position_z"),
    ) @AttributeOverride(
        name = "y",
        column = Column(name = "position_y"),
    ) @AttributeOverride(
        name = "x",
        column = Column(name = "position_x"),
    ) @Embedded var position: Spatial? = Spatial(0.0, 0.0, 0.0),
    @AttributeOverride(
        name = "z",
        column = Column(name = "rotation_z"),
    ) @AttributeOverride(
        name = "y",
        column = Column(name = "rotation_y"),
    ) @AttributeOverride(
        name = "x",
        column = Column(name = "rotation_x"),
    ) @Embedded var rotation: Spatial? = Spatial(0.0, 0.0, 0.0),
) : IdentityIdEntity() {
    fun addSceneDeviceAction(sceneDeviceAction: SceneDeviceAction) {
        sceneDeviceActions.add(sceneDeviceAction)
        sceneDeviceAction.scene = this
    }

    fun removeSceneDeviceAction(sceneDeviceAction: SceneDeviceAction) {
        sceneDeviceActions.remove(sceneDeviceAction)
    }
}
