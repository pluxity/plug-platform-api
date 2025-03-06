package com.pluxity.building.entity;

import com.pluxity.permission.entity.Permission;
import com.pluxity.user.entity.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "building_permission")
@DiscriminatorValue("BUILDING")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BuildingPermission extends Permission {

    @Builder
    public BuildingPermission(Role role, Long buildingId) {
        super(role, buildingId);
    }

    @Override
    public boolean hasPermission(Long buildingId) {
        if ("ADMIN".equals(getRole().getRoleName())) {
            return true;
        }
        
        return getResourceId() != null && getResourceId().equals(buildingId);
    }
} 