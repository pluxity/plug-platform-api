package com.pluxity.domains.device.entity;

import com.pluxity.feature.entity.Feature;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "label_3d")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Label3D extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @Column(name = "text_content")
    private String textContent;

    @Builder
    public Label3D(Long id, Feature feature, String textContent) {
        this.id = id;
        this.feature = feature;
        if (this.feature != null) {
            this.feature.changeDevice(null);
        }
        this.textContent = textContent;
    }

    public static Label3D create(String textContent) {
        return Label3D.builder().textContent(textContent).build();
    }

    public void update(String textContent) {
        if (textContent != null) {
            this.textContent = textContent;
        }
    }

    public void changeFeature(Feature feature) {
        if (this.feature != null && this.feature != feature) {
            this.feature.changeDevice(null);
        }

        this.feature = feature;

        if (feature != null) {
            feature.changeDevice(null);
        }
    }

    public void clearFeatureOnly() {
        this.feature = null;
    }

    public void clearAllRelations() {
        if (this.feature != null) {
            this.changeFeature(null);
        }
    }
}
