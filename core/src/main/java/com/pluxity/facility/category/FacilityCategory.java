package com.pluxity.facility.category;

import com.pluxity.category.entity.Category;
import com.pluxity.facility.facility.Facility;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
