package com.pluxity.facility.entity;

import com.pluxity.category.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facility_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FacilityCategory extends Category<FacilityCategory> {

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Facility> facilities = new ArrayList<>();

    @Column(name = "image_file_id")
    private Long imageFileId;

    @Builder
    public FacilityCategory(String name, FacilityCategory parent) {
        this.name = name;
        this.parent = parent;
        this.validateDepth();
    }

    public void updateImageFile(Long imageFileId) {
        this.imageFileId = imageFileId;
    }

    @Override
    public int getMaxDepth() {
        return 3;
    }
}