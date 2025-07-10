package com.pluxity.facility.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityCategoryRepository extends JpaRepository<FacilityCategory, Long> {
    Optional<FacilityCategory> findByNameAndParentId(String name, Long parentId);

    List<FacilityCategory> findAllByParentIsNull();
}
