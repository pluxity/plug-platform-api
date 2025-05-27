package com.pluxity.domains.device.repository;

import com.pluxity.domains.device.entity.NfluxCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NfluxCategoryRepository extends JpaRepository<NfluxCategory, Long> {

    @Query("SELECT n FROM NfluxCategory n WHERE n.parent IS NULL")
    List<NfluxCategory> findAllRootCategories();

    List<NfluxCategory> findByParentId(Long parentId);
}
