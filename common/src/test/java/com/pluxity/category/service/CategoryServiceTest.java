package com.pluxity.category.service;

import com.pluxity.category.dto.CategoryCreateRequest;
import com.pluxity.category.dto.CategoryResponseC;
import com.pluxity.category.dto.CategoryUpdateRequest;
import com.pluxity.category.entity.Category;
import com.pluxity.category.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category parentCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        parentCategory = Category.builder()
                .id(1L)
                .name("부모 카테고리")
                .description("부모 카테고리 설명")
                .build();

        childCategory = Category.builder()
                .id(2L)
                .name("자식 카테고리")
                .description("자식 카테고리 설명")
                .build();
    }

    @Test
    void 카테고리_생성_테스트() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest(
                "새 카테고리",
                "새 카테고리 설명",
                null
        );
        Category newCategory = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();
        given(categoryRepository.save(any(Category.class))).willReturn(newCategory);

        // when
        CategoryResponseC response = categoryService.create(request);

        // then
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.description()).isEqualTo(request.description());
        assertThat(response.parentId()).isNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void 부모_카테고리가_있는_카테고리_생성_테스트() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest(
                "새 카테고리",
                "새 카테고리 설명",
                1L
        );
        given(categoryRepository.findById(1L)).willReturn(Optional.of(parentCategory));
        given(categoryRepository.save(any(Category.class))).willReturn(childCategory);

        // when
        CategoryResponseC response = categoryService.create(request);

        // then
        assertThat(response.name()).isEqualTo(childCategory.getName());
        assertThat(response.parentId()).isEqualTo(parentCategory.getId());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void 카테고리_조회_테스트() {
        // given
        given(categoryRepository.findById(1L)).willReturn(Optional.of(parentCategory));

        // when
        CategoryResponseC response = categoryService.findById(1L);

        // then
        assertThat(response.id()).isEqualTo(parentCategory.getId());
        assertThat(response.name()).isEqualTo(parentCategory.getName());
    }

    @Test
    void 존재하지_않는_카테고리_조회_테스트() {
        // given
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.findById(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Category not found with id: 1");
    }

    @Test
    void 카테고리_수정_테스트() {
        // given
        CategoryUpdateRequest request = new CategoryUpdateRequest(
                "수정된 카테고리",
                "수정된 설명"
        );
        given(categoryRepository.findById(1L)).willReturn(Optional.of(parentCategory));
        given(categoryRepository.save(any(Category.class))).willReturn(parentCategory);

        // when
        CategoryResponseC response = categoryService.update(1L, request);

        // then
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.description()).isEqualTo(request.description());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void 카테고리_삭제_테스트() {
        // given
        Category leafCategory = Category.builder()
                .id(3L)
                .name("리프 카테고리")
                .build();
        given(categoryRepository.findById(3L)).willReturn(Optional.of(leafCategory));

        // when
        categoryService.delete(3L);

        // then
        verify(categoryRepository).delete(leafCategory);
    }

    @Test
    void 자식이_있는_카테고리_삭제_시도_테스트() {
        // given
        parentCategory.addChild(childCategory);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(parentCategory));

        // when & then
        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot delete category with children");
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void 카테고리_이동_테스트() {
        // given
        Category newParent = Category.builder()
                .id(3L)
                .name("새 부모 카테고리")
                .build();
        given(categoryRepository.findById(2L)).willReturn(Optional.of(childCategory));
        given(categoryRepository.findById(3L)).willReturn(Optional.of(newParent));
        given(categoryRepository.save(any(Category.class))).willReturn(childCategory);

        // when
        CategoryResponseC response = categoryService.move(2L, 3L);

        // then
        assertThat(response.parentId()).isEqualTo(newParent.getId());
        verify(categoryRepository).save(childCategory);
    }
} 