package com.pluxity.feature.entity;

import com.pluxity.device.entity.Device;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "feature")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Feature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @OneToOne(mappedBy = "feature", cascade = CascadeType.PERSIST)
    private Device device;

    @Builder
    public Feature(Long id, Spatial position, Spatial rotation, Spatial scale) {
        this.id = id;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
    }

    public static Feature create(FeatureCreateRequest request) {
        return Feature.builder()
                .position(request.position())
                .rotation(request.rotation())
                .scale(request.scale())
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

    public void updatePosition(Spatial position) {
        if (position != null) {
            this.position = position;
        }
    }

    public void updateRotation(Spatial rotation) {
        if (rotation != null) {
            this.rotation = rotation;
        }
    }

    public void updateScale(Spatial scale) {
        if (scale != null) {
            this.scale = scale;
        }
    }

    public boolean isDefaultPosition() {
        return position != null
                && position.getX() == 0.0
                && position.getY() == 0.0
                && position.getZ() == 0.0;
    }

    public boolean isDefaultScale() {
        return scale != null && scale.getX() == 1.0 && scale.getY() == 1.0 && scale.getZ() == 1.0;
    }

    public boolean isDefaultRotation() {
        return rotation != null
                && rotation.getX() == 0.0
                && rotation.getY() == 0.0
                && rotation.getZ() == 0.0;
    }

    public void changeDevice(Device device) {
        this.device = device;
    }
}
