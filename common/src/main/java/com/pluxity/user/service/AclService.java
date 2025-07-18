package com.pluxity.user.service;

import com.pluxity.facility.category.FacilityCategory;
import com.pluxity.facility.category.FacilityCategoryRepository;
import com.pluxity.global.annotation.CheckPermissionCategory;
import com.pluxity.user.entity.ResourceType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AclService {
    private final FacilityCategoryRepository facilityCategoryRepository;

    @Transactional(readOnly = true)
    @CheckPermissionCategory(categoryResourceType = ResourceType.FACILITY_CATEGORY)
    public List<FacilityCategory> facilityCategories() {
        return facilityCategoryRepository.findAll();
    }
}
