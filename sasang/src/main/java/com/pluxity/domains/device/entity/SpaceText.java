package com.pluxity.domains.device.entity;

import com.pluxity.feature.entity.Feature;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "space_text")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpaceText extends BaseEntity {

    @Id private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @Column(name = "text_content")
    private String textContent;

    @Builder
    public SpaceText(String id, Feature feature, String textContent) {
        this.id = id;
        this.feature = feature;
        if (this.feature != null) {
            this.feature.changeDevice(null);
        }
        this.textContent = textContent;
    }

    public static SpaceText create(String id, String textContent) {
        return SpaceText.builder().id(id).textContent(textContent).build();
    }

    public void update(String TextContent) {
        if (TextContent != null) {
            this.textContent = TextContent;
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
