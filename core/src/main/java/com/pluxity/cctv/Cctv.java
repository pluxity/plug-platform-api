package com.pluxity.cctv;

import com.pluxity.cctv.category.CctvCategory;
import com.pluxity.feature.entity.Feature;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cctv {
    @Id private String id;
    private String name;
    private String url;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CctvCategory category;
}
