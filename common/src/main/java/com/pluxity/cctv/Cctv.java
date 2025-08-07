package com.pluxity.cctv;

import com.pluxity.cctv.category.CctvCategory;
import com.pluxity.cctv.dto.CctvUpdateRequest;
import com.pluxity.feature.entity.Feature;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cctv {
    @Id private String id;
    private String name;

    @Column(length = 1000)
    private String url;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CctvCategory category;

    @Builder
    public Cctv(String id, String name, String url, CctvCategory category, Feature feature) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.category = category;
        this.feature = feature;
    }

    public void updateCctv(CctvUpdateRequest request) {
        this.name = request.name();
        this.url = request.url();
    }

    public void changeFeature(Feature feature) {
        this.feature = feature;
    }

    public void changeCategory(CctvCategory category) {
        this.category = category;
    }
}
