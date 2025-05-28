package com.pluxity.asset.entity;

import com.pluxity.file.entity.FileEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssetTest {

    @Test
    @DisplayName("자산을 생성할 수 있다")
    void createAsset() {
        // given
        String name = "테스트 자산";
        Long fileId = 1L;
        Long thumbnailFileId = 2L;

        // when
        Asset asset = Asset.builder()
                .name(name)
                .fileId(fileId)
                .thumbnailFileId(thumbnailFileId)
                .build();

        // then
        assertThat(asset.getName()).isEqualTo(name);
        assertThat(asset.getFileId()).isEqualTo(fileId);
        assertThat(asset.getThumbnailFileId()).isEqualTo(thumbnailFileId);
        assertThat(asset.getCategory()).isNull();
    }

    @Test
    @DisplayName("자산을 카테고리와 함께 생성할 수 있다")
    void createAssetWithCategory() {
        // given
        String name = "테스트 자산";
        AssetCategory category = AssetCategory.builder()
                .name("테스트 카테고리")
                .build();

        // when
        Asset asset = Asset.builder()
                .name(name)
                .category(category)
                .build();

        // then
        assertThat(asset.getName()).isEqualTo(name);
        assertThat(asset.getCategory()).isEqualTo(category);
        assertThat(category.getAssets()).contains(asset);
    }

    @Test
    @DisplayName("자산의 카테고리를 업데이트할 수 있다")
    void updateCategory() {
        // given
        Asset asset = Asset.builder()
                .name("테스트 자산")
                .build();
        AssetCategory oldCategory = AssetCategory.builder()
                .name("기존 카테고리")
                .build();
        asset.updateCategory(oldCategory);
        
        AssetCategory newCategory = AssetCategory.builder()
                .name("새 카테고리")
                .build();
        
        // when
        asset.updateCategory(newCategory);
        
        // then
        assertThat(asset.getCategory()).isEqualTo(newCategory);
        assertThat(oldCategory.getAssets()).doesNotContain(asset);
        assertThat(newCategory.getAssets()).contains(asset);
    }

    @Test
    @DisplayName("자산의 카테고리를 null로 설정할 수 있다")
    void updateCategoryToNull() {
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
        asset.updateCategory(null);
        
        // then
        assertThat(asset.getCategory()).isNull();
        assertThat(category.getAssets()).doesNotContain(asset);
    }

    @Test
    @DisplayName("파일 ID 및 썸네일 파일 ID 업데이트 테스트")
    void updateFileIds() {
        // given
        Asset asset = Asset.builder()
                .name("테스트 자산")
                .build();
        Long fileId = 10L;
        Long thumbnailFileId = 20L;
        
        // when
        asset.updateFileId(fileId);
        asset.updateThumbnailFileId(thumbnailFileId);
        
        // then
        assertThat(asset.getFileId()).isEqualTo(fileId);
        assertThat(asset.getThumbnailFileId()).isEqualTo(thumbnailFileId);
        assertThat(asset.hasFile()).isTrue();
        assertThat(asset.hasThumbnail()).isTrue();
    }

    @Test
    @DisplayName("파일 엔티티로 파일 ID 업데이트 테스트")
    void updateFileEntities() {
        // given
        Asset asset = Asset.builder()
                .name("테스트 자산")
                .build();
        
        // Mock FileEntity 객체 생성
        FileEntity fileEntity = mock(FileEntity.class);
        when(fileEntity.getId()).thenReturn(30L);
        
        FileEntity thumbnailEntity = mock(FileEntity.class);
        when(thumbnailEntity.getId()).thenReturn(40L);
        
        // when
        asset.updateFileEntity(fileEntity);
        asset.updateThumbnailFileEntity(thumbnailEntity);
        
        // then
        assertThat(asset.getFileId()).isEqualTo(fileEntity.getId());
        assertThat(asset.getThumbnailFileId()).isEqualTo(thumbnailEntity.getId());
    }
}