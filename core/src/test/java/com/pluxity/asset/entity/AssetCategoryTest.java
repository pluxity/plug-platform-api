package com.pluxity.asset.entity;

import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetCategoryTest {

    @Test
    @DisplayName("자산 카테고리를 생성할 수 있다")
    void createAssetCategory() {
        // given
        String name = "테스트 카테고리";

        // when
        AssetCategory category = AssetCategory.builder()
                .name(name)
                .build();

        // then
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getParent()).isNull();
        assertThat(category.getChildren()).isEmpty();
        assertThat(category.getAssets()).isEmpty();
        assertThat(category.getIconFileId()).isNull();
    }

//    @Test
//    @DisplayName("자산 카테고리를 부모 카테고리와 함께 생성할 수 있다")
//    void createAssetCategoryWithParent() {
//        // given
//        String parentName = "부모 카테고리";
//        String childName = "자식 카테고리";
//        AssetCategory parent = AssetCategory.builder()
//                .name(parentName)
//                .build();
//
//        // when
//        AssetCategory child = AssetCategory.builder()
//                .name(childName)
//                .parent(parent)
//                .build();
//
//        // then
//        assertThat(child.getName()).isEqualTo(childName);
//        assertThat(child.getParent()).isEqualTo(parent);
//        assertThat(parent.getChildren()).contains(child);
//        assertThat(child.getDepth()).isEqualTo(parent.getDepth() + 1);
//    }

    @Test
    @DisplayName("최대 깊이를 초과하는 자산 카테고리를 생성하면 예외가 발생한다")
    void createAssetCategoryExceedingMaxDepth() {
        // given
        AssetCategory level1 = AssetCategory.builder()
                .name("레벨 1")
                .build();

        // when & then
        assertThatThrownBy(() -> AssetCategory.builder()
                .name("레벨 4")
                .parent(level1)
                .build())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("카테고리는 깊이를 초과");
    }

    @Test
    @DisplayName("자산 카테고리에 아이콘 파일 ID를 업데이트할 수 있다")
    void updateIconFileId() {
        // given
        AssetCategory category = AssetCategory.builder()
                .name("테스트 카테고리")
                .build();
        Long iconFileId = 123L;

        // when
        category.updateIconFileId(iconFileId);

        // then
        assertThat(category.getIconFileId()).isEqualTo(iconFileId);
    }

    @Test
    @DisplayName("자산 카테고리에 자산을 추가할 수 있다")
    void addAsset() {
        // given
        AssetCategory category = AssetCategory.builder()
                .name("테스트 카테고리")
                .build();
        Asset asset = Asset.builder()
                .name("테스트 자산")
                .build();

        // when
        category.addAsset(asset);

        // then
        assertThat(category.getAssets()).contains(asset);
    }

    @Test
    @DisplayName("자산 카테고리에서 자산을 제거할 수 있다")
    void removeAsset() {
        // given
        AssetCategory category = AssetCategory.builder()
                .name("테스트 카테고리")
                .build();
        Asset asset = Asset.builder()
                .name("테스트 자산")
                .category(category)
                .build();
        assertThat(category.getAssets()).contains(asset);

        // when
        category.removeAsset(asset);

        // then
        assertThat(category.getAssets()).doesNotContain(asset);
    }

    @Test
    @DisplayName("null 자산은 추가되지 않는다")
    void addNullAsset() {
        // given
        AssetCategory category = AssetCategory.builder()
                .name("테스트 카테고리")
                .build();
        int initialSize = category.getAssets().size();

        // when
        category.addAsset(null);

        // then
        assertThat(category.getAssets().size()).isEqualTo(initialSize);
    }

    @Test
    @DisplayName("이미 추가된 자산은 중복 추가되지 않는다")
    void addDuplicateAsset() {
        // given
        AssetCategory category = AssetCategory.builder()
                .name("테스트 카테고리")
                .build();
        Asset asset = Asset.builder()
                .name("테스트 자산")
                .category(category)
                .build();
        int initialSize = category.getAssets().size();

        // when
        category.addAsset(asset);

        // then
        assertThat(category.getAssets().size()).isEqualTo(initialSize);
    }
} 