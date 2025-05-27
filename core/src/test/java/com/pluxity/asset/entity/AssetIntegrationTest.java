package com.pluxity.asset.entity;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AssetIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("자산을 저장하고 조회할 수 있다")
    void saveAndFindAsset() {
        // given
        Asset asset = Asset.builder()
                .name("테스트 자산")
                .fileId(1L)
                .thumbnailFileId(2L)
                .build();

        // when
        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        // then
        Asset foundAsset = entityManager.find(Asset.class, asset.getId());
        assertThat(foundAsset).isNotNull();
        assertThat(foundAsset.getName()).isEqualTo("테스트 자산");
        assertThat(foundAsset.getFileId()).isEqualTo(1L);
        assertThat(foundAsset.getThumbnailFileId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("자산과 카테고리 간의 관계를 저장하고 조회할 수 있다")
    void saveAndFindAssetWithCategory() {
        // given
        AssetCategory category = AssetCategory.builder()
                .name("테스트 카테고리")
                .build();
        entityManager.persist(category);

        Asset asset = Asset.builder()
                .name("테스트 자산")
                .category(category)
                .build();
        entityManager.persist(asset);
        entityManager.flush();
        entityManager.clear();

        // when
        Asset foundAsset = entityManager.find(Asset.class, asset.getId());

        // then
        assertThat(foundAsset).isNotNull();
        assertThat(foundAsset.getCategory()).isNotNull();
        assertThat(foundAsset.getCategory().getName()).isEqualTo("테스트 카테고리");
    }

    @Test
    @DisplayName("자산의 카테고리를 변경할 수 있다")
    void updateAssetCategory() {
        // given
        AssetCategory oldCategory = AssetCategory.builder()
                .name("기존 카테고리")
                .build();
        entityManager.persist(oldCategory);

        Asset asset = Asset.builder()
                .name("테스트 자산")
                .category(oldCategory)
                .build();
        entityManager.persist(asset);

        AssetCategory newCategory = AssetCategory.builder()
                .name("새 카테고리")
                .build();
        entityManager.persist(newCategory);
        entityManager.flush();

        // when
        asset.updateCategory(newCategory);
        entityManager.flush();
        entityManager.clear();

        // then
        Asset foundAsset = entityManager.find(Asset.class, asset.getId());
        AssetCategory foundOldCategory = entityManager.find(AssetCategory.class, oldCategory.getId());
        AssetCategory foundNewCategory = entityManager.find(AssetCategory.class, newCategory.getId());

        assertThat(foundAsset.getCategory().getId()).isEqualTo(newCategory.getId());
        assertThat(foundOldCategory.getAssets()).isEmpty();
        assertThat(foundNewCategory.getAssets()).hasSize(1);
        assertThat(foundNewCategory.getAssets().get(0).getId()).isEqualTo(asset.getId());
    }

//    @Test
//    @DisplayName("자산의 카테고리를 null로 설정할 수 있다")
//    void removeAssetCategory() {
//        // given
//        AssetCategory category = AssetCategory.builder()
//                .name("테스트 카테고리")
//                .code("TC1")
//                .build();
//        entityManager.persist(category);
//        entityManager.flush();
//
//        Asset asset = Asset.builder()
//                .name("테스트 자산")
//                .code("AS1")
//                .category(category)
//                .build();
//        entityManager.persist(asset);
//        entityManager.flush();
//
//        // when
//        Asset persistedAsset = entityManager.find(Asset.class, asset.getId());
//        assertThat(persistedAsset).isNotNull();
//        assertThat(persistedAsset.getCategory()).isNotNull();
//
//        persistedAsset.updateCategory(null);
//        entityManager.flush();
//
//        // then
//        Asset foundAsset = entityManager.find(Asset.class, asset.getId());
//        assertThat(foundAsset).isNotNull();
//        assertThat(foundAsset.getCategory()).isNull();
//
//        AssetCategory foundCategory = entityManager.find(AssetCategory.class, category.getId());
//        assertThat(foundCategory).isNotNull();
//        assertThat(foundCategory.getAssets()).isEmpty();
//    }

    @Test
    @DisplayName("자산의 이름과 파일 ID를 업데이트할 수 있다")
    void updateAssetProperties() {
        // given
        Asset asset = Asset.builder()
                .name("기존 자산")
                .build();
        entityManager.persist(asset);
        entityManager.flush();

        // when
        asset.update("새 자산 이름");
        asset.updateFileId(10L);
        asset.updateThumbnailFileId(20L);
        entityManager.flush();
        entityManager.clear();

        // then
        Asset foundAsset = entityManager.find(Asset.class, asset.getId());
        assertThat(foundAsset.getName()).isEqualTo("새 자산 이름");
        assertThat(foundAsset.getFileId()).isEqualTo(10L);
        assertThat(foundAsset.getThumbnailFileId()).isEqualTo(20L);
    }
} 