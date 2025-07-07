package com.pluxity.asset.repository;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.entity.AssetCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByCategory(AssetCategory category);

    Optional<Asset> findByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<Asset> findByName(String name);

    Optional<Asset> findByNameAndIdNot(String name, Long id);

    Optional<Asset> findByCodeAndIdNot(String code, Long id);
}
