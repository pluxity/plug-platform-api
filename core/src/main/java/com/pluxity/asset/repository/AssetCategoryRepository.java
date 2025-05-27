package com.pluxity.asset.repository;

import com.pluxity.asset.entity.AssetCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {

    Optional<AssetCategory> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT ac FROM AssetCategory ac WHERE ac.parent IS NULL")
    List<AssetCategory> findAllRootCategories();

    List<AssetCategory> findByParentId(Long parentId);

    @Query("SELECT ac FROM AssetCategory ac LEFT JOIN FETCH ac.assets WHERE ac.id = :id")
    Optional<AssetCategory> findByIdWithAssets(Long id);
}
