package com.pluxity.label3d;

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

    @Id private String id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @Column(name = "text_content")
    private String displayText;

    @Builder
    public Label3D(Feature feature, String displayText) {
        this.feature = feature;
        this.displayText = displayText;
        if (this.feature != null) {
            this.feature.changeDevice(null);
        }
    }

    public static Label3D create(String displayText) {
        return Label3D.builder().displayText(displayText).build();
    }

    public static Label3D createWithFeature(Feature feature, String displayText) {
        return Label3D.builder().feature(feature).displayText(displayText).build();
    }

    public void update(String displayText) {
        if (displayText != null) {
            this.displayText = displayText;
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
