package com.pluxity.feature.entity;

import com.pluxity.facility.Facility;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "feature")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Feature extends BaseEntity {

    @Id private String id;

    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "position_x"))
    @AttributeOverride(name = "y", column = @Column(name = "position_y"))
    @AttributeOverride(name = "z", column = @Column(name = "position_z"))
    private Spatial position;

    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "rotation_x"))
    @AttributeOverride(name = "y", column = @Column(name = "rotation_y"))
    @AttributeOverride(name = "z", column = @Column(name = "rotation_z"))
    private Spatial rotation;

    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "scale_x"))
    @AttributeOverride(name = "y", column = @Column(name = "scale_y"))
    @AttributeOverride(name = "z", column = @Column(name = "scale_z"))
    private Spatial scale;

    private Long assetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Facility facility;

    @Column(name = "floor_id")
    private String floorId;

    @Builder
    public Feature(
            String id,
            Spatial position,
            Spatial rotation,
            Spatial scale,
            Long assetId,
            Facility facility,
            String floorId) {
        this.id = id;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.floorId = floorId;
        this.assetId = assetId;
        this.facility = facility;
    }

    public static Feature create(FeatureCreateRequest request, String uuid, Facility facility) {
        return Feature.builder()
                .id(uuid)
                .position(request.position() != null ? request.position() : new Spatial(0.0, 0.0, 0.0))
                .rotation(request.rotation() != null ? request.rotation() : new Spatial(0.0, 0.0, 0.0))
                .scale(request.scale() != null ? request.scale() : new Spatial(1.0, 1.0, 1.0))
                .assetId(request.assetId())
                .floorId(request.floorId())
                .facility(facility)
                .build();
    }

    public void update(FeatureUpdateRequest request) {
        if (request.position() != null) {
            this.position = request.position();
        }
        if (request.rotation() != null) {
            this.rotation = request.rotation();
        }
        if (request.scale() != null) {
            this.scale = request.scale();
        }
    }
}
