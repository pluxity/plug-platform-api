package com.pluxity.facility.entity;

import com.pluxity.category.entity.Category;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facility_category")
@Getter
@NoArgsConstructor
public class FacilityCategory extends Category<FacilityCategory> {

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Facility> facilities = new ArrayList<>();

    @Builder
    public FacilityCategory(String name, FacilityCategory parent) {
        this.name = name;
        this.parent = parent;
        this.validateDepth();
    }

    @Override
    public int getMaxDepth() {
        return 3;
    }
}