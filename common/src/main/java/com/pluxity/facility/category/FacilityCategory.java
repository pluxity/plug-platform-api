package com.pluxity.facility.category;

import com.pluxity.category.entity.Category;
import com.pluxity.facility.Facility;
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

    @OneToMany(mappedBy = "category")
    private final List<Facility> facilities = new ArrayList<>();

    @Builder
    public FacilityCategory(String name, FacilityCategory parent) {
        this.name = name;
        if (parent != null) {
            this.assignToParent(parent);
        } else {
            this.parent = null;
        }
        this.validateDepth();
    }
}
